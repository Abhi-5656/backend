package com.wfm.experts.entity.tenant.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Added for convenience
import lombok.AllArgsConstructor; // Added for convenience
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor // Lombok: Adds a no-argument constructor
@AllArgsConstructor // Lombok: Adds an all-argument constructor
@Entity
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}), // Work Email
                @UniqueConstraint(columnNames = {"employee_id"}),
                @UniqueConstraint(columnNames = {"phone_number"}) // Primary/Work Mobile Number
                // Unique constraints for fields within PersonalInfo (like pan_number, aadhaar_number)
                // are defined with @Column(unique=true) in PersonalInfo.java and apply to the employees table.
        })
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Core Identifying and Auth Info ---
    @NotBlank(message = "Employee Code (ID) is required")
    @Length(max = 50, message = "Employee ID must not exceed 50 characters")
    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId; // Maps to "Employee Code *"

    @NotBlank(message = "Work Email ID is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true)
    private String email; // Maps to "Work Email *", also used as username for login

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    @JsonIgnore // Prevent password from being serialized in API responses
    private String password; // Maps to "Password *"

    @NotBlank(message = "Primary Mobile Number is required")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid mobile number format for primary mobile")
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber; // Primary work mobile number

    // --- System Role ---
    @NotNull(message = "System Role is required")
    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch as role is often needed
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Maps to "Role *" (System Role: ADMIN, EMPLOYEE, HR, etc.)

    // --- Personal Information (Embedded) ---
    // Contains fields like first name, last name, DOB, gender, addresses, emergency contact, etc.
    @Embedded
    private PersonalInfo personalInfo;

    // --- Organizational Structure & Job Relationships (Direct Relationships on Employee) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_location_id") // Foreign Key to locations table
    private Location workLocation; // Maps to "Work Location *"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id") // Foreign Key to business_units table
    private BusinessUnit businessUnit; // Maps to "Business Unit *"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id") // Foreign Key to job_titles table
    private JobTitle jobTitle; // Maps to "Designation *"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id") // Foreign Key (Self-referential to employees table)
    @NotFound(action = NotFoundAction.IGNORE) // Avoids issues if manager is null or deleted
    private Employee reportingManager; // Maps to "Reporting Manager *"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_manager_id") // Foreign Key (Self-referential to employees table)
    @NotFound(action = NotFoundAction.IGNORE) // Avoids issues if HR manager is null or deleted
    private Employee hrManager; // Maps to "HR Manager"

    // --- Organizational Information (Embedded Wrapper) ---
    // This OrganizationalInfo object will contain the EmploymentDetails
    // (date of joining, employment type, status, department, job grade, etc.)
    @Embedded
    private OrganizationalInfo organizationalInfo;


    // --- Tenant and Timestamps ---
    @JsonIgnore // Typically not sent in API responses directly
    @Column(name = "tenant_id")
    private String tenantId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false) // Automatically set on creation
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at") // Automatically set on update
    private Date updatedAt;


    // --- Lifecycle Callbacks for derived fields and timestamps ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();

        // Populate derived fields in PersonalInfo (fullName, displayName)
        // and handle "permanent address same as current" logic
        if (this.personalInfo != null) {
            String firstName = this.personalInfo.getFirstName() != null ? this.personalInfo.getFirstName().trim() : "";
            String middleNamePart = (this.personalInfo.getMiddleName() != null && !this.personalInfo.getMiddleName().trim().isEmpty())
                    ? this.personalInfo.getMiddleName().trim() + " " : "";
            String lastName = this.personalInfo.getLastName() != null ? this.personalInfo.getLastName().trim() : "";

            this.personalInfo.setFullName((firstName + " " + middleNamePart + lastName).trim().replaceAll("\\s+", " "));

            if (this.personalInfo.getDisplayName() == null || this.personalInfo.getDisplayName().trim().isEmpty()) {
                this.personalInfo.setDisplayName((firstName + " " + lastName).trim().replaceAll("\\s+", " "));
            }

            // Auto-fill permanent address if "same as current" is true and current address is provided
            if (this.personalInfo.isPermanentSameAsCurrent() && this.personalInfo.getCurrentAddress() != null) {
                if (this.personalInfo.getPermanentAddress() == null) {
                    this.personalInfo.setPermanentAddress(new Address()); // Initialize if null
                }
                // Copy only if permanent address fields seem uninitialized to avoid overwriting manually entered data
                // if the flag was toggled.
                boolean permanentAddressSeemsUninitialized =
                        (this.personalInfo.getPermanentAddress().getAddressLine1() == null || this.personalInfo.getPermanentAddress().getAddressLine1().trim().isEmpty()) &&
                                (this.personalInfo.getPermanentAddress().getCity() == null || this.personalInfo.getPermanentAddress().getCity().trim().isEmpty()) &&
                                (this.personalInfo.getPermanentAddress().getState() == null || this.personalInfo.getPermanentAddress().getState().trim().isEmpty()) &&
                                (this.personalInfo.getPermanentAddress().getPincode() == null || this.personalInfo.getPermanentAddress().getPincode().trim().isEmpty());

                if (permanentAddressSeemsUninitialized) {
                    this.personalInfo.getPermanentAddress().setAddressLine1(this.personalInfo.getCurrentAddress().getAddressLine1());
                    this.personalInfo.getPermanentAddress().setAddressLine2(this.personalInfo.getCurrentAddress().getAddressLine2());
                    this.personalInfo.getPermanentAddress().setCity(this.personalInfo.getCurrentAddress().getCity());
                    this.personalInfo.getPermanentAddress().setState(this.personalInfo.getCurrentAddress().getState());
                    this.personalInfo.getPermanentAddress().setPincode(this.personalInfo.getCurrentAddress().getPincode());
                }
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
        // Re-populate derived fields in PersonalInfo in case names change
        if (this.personalInfo != null) {
            String firstName = this.personalInfo.getFirstName() != null ? this.personalInfo.getFirstName().trim() : "";
            String middleNamePart = (this.personalInfo.getMiddleName() != null && !this.personalInfo.getMiddleName().trim().isEmpty())
                    ? this.personalInfo.getMiddleName().trim() + " " : "";
            String lastName = this.personalInfo.getLastName() != null ? this.personalInfo.getLastName().trim() : "";

            this.personalInfo.setFullName((firstName + " " + middleNamePart + lastName).trim().replaceAll("\\s+", " "));

            // Only update display name if it was originally derived based on first+last or is now empty
            String simpleDerivedDisplayName = ((firstName + " " + lastName).trim().replaceAll("\\s+", " "));
            if (this.personalInfo.getDisplayName() == null ||
                    this.personalInfo.getDisplayName().trim().isEmpty() ||
                    this.personalInfo.getDisplayName().equals(simpleDerivedDisplayName) ) {
                this.personalInfo.setDisplayName(simpleDerivedDisplayName);
            }

            // Auto-fill or clear permanent address based on "same as current"
            if (this.personalInfo.isPermanentSameAsCurrent() && this.personalInfo.getCurrentAddress() != null) {
                if (this.personalInfo.getPermanentAddress() == null) {
                    this.personalInfo.setPermanentAddress(new Address()); // Initialize if null
                }
                this.personalInfo.getPermanentAddress().setAddressLine1(this.personalInfo.getCurrentAddress().getAddressLine1());
                this.personalInfo.getPermanentAddress().setAddressLine2(this.personalInfo.getCurrentAddress().getAddressLine2());
                this.personalInfo.getPermanentAddress().setCity(this.personalInfo.getCurrentAddress().getCity());
                this.personalInfo.getPermanentAddress().setState(this.personalInfo.getCurrentAddress().getState());
                this.personalInfo.getPermanentAddress().setPincode(this.personalInfo.getCurrentAddress().getPincode());
            }
            // If !isPermanentSameAsCurrent, the client is responsible for providing the permanent address.
            // We don't automatically clear it here to avoid data loss if the user unchecks by mistake.
        }
    }
}

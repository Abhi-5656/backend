package com.wfm.experts.tenant.common.employees.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wfm.experts.tenant.common.employees.enums.BloodGroup;
import com.wfm.experts.tenant.common.employees.enums.Gender;
import com.wfm.experts.tenant.common.employees.enums.MaritalStatus;
import com.wfm.experts.validation.groups.OnEmployeeProfile; // Import group
// Default group is implicitly part of OnAdminCreation if it extends Default
// import com.wfm.experts.validation.groups.OnAdminCreation; // Not strictly needed for annotations if relying on Default
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default; // Important for fields always required
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_personal_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PersonalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // These are provided during admin creation and are always fundamental
    @NotBlank(message = "First Name is required", groups = {Default.class, OnEmployeeProfile.class}) // Default applies to OnAdminCreation
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name") // Optional
    private String middleName;

    @NotBlank(message = "Last Name is required", groups = {Default.class, OnEmployeeProfile.class}) // Default applies to OnAdminCreation
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "display_name")
    private String displayName;

    // These fields are ONLY required for a full employee profile, NOT for initial admin creation
    @NotNull(message = "Gender is required", groups = OnEmployeeProfile.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "gender") // Database column must be nullable
    private Gender gender;

    @NotNull(message = "Date of Birth is required", groups = OnEmployeeProfile.class)
    @Past(message = "Date of Birth must be in the past", groups = OnEmployeeProfile.class)
    @Column(name = "date_of_birth") // Database column must be nullable
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group") // Optional
    private BloodGroup bloodGroup;

    @NotNull(message = "Marital Status is required", groups = OnEmployeeProfile.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status") // Database column must be nullable
    private MaritalStatus maritalStatus;

    @NotBlank(message = "PAN Number is required", groups = OnEmployeeProfile.class)
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN number format", groups = OnEmployeeProfile.class)
    @Column(name = "pan_number", unique = true) // Database column must be nullable for admin creation
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is required", groups = OnEmployeeProfile.class)
    @Pattern(regexp = "^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$", message = "Invalid Aadhaar number format", groups = OnEmployeeProfile.class)
    @Column(name = "aadhaar_number", unique = true) // Database column must be nullable for admin creation
    private String aadhaarNumber;

    @NotBlank(message = "Nationality is required", groups = OnEmployeeProfile.class)
    @Column(name = "nationality") // Database column must be nullable
    private String nationality;

    @jakarta.validation.constraints.Email(message = "Invalid personal email format", groups = OnEmployeeProfile.class)
    @Column(name = "personal_email", unique = true) // Optional for admin, potentially required for full profile
    private String personalEmail;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid alternate mobile number format")
    @Column(name = "alternate_mobile") // Optional
    private String alternateMobile;

    @Embedded @Valid // Validates EmergencyContact based on its own annotations (Default group)
    private EmergencyContact emergencyContact;

    @Embedded @Valid // Validates Address based on its own annotations (Default group)
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine1", column = @Column(name = "current_address_line_1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "current_address_line_2")),
            @AttributeOverride(name = "state", column = @Column(name = "current_state")),
            @AttributeOverride(name = "city", column = @Column(name = "current_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "current_pincode"))
    })
    private Address currentAddress;

    @Column(name = "is_permanent_same_as_current")
    private boolean permanentSameAsCurrent = false;

    @Embedded @Valid // Validates Address based on its own annotations (Default group)
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine1", column = @Column(name = "permanent_address_line_1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "permanent_address_line_2")),
            @AttributeOverride(name = "state", column = @Column(name = "permanent_state")),
            @AttributeOverride(name = "city", column = @Column(name = "permanent_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "permanent_pincode"))
    })
    private Address permanentAddress;

    @PrePersist
    @PreUpdate
    private void deriveFields() {
        // ... (derivation logic as before) ...
        String first = (this.firstName != null ? this.firstName.trim() : "");
        String middle = (this.middleName != null && !this.middleName.trim().isEmpty() ? this.middleName.trim() + " " : "");
        String last = (this.lastName != null ? this.lastName.trim() : "");

        this.fullName = (first + " " + middle + last).trim().replaceAll("\\s+", " ");
        if (this.displayName == null || this.displayName.trim().isEmpty()) {
            this.displayName = (first + " " + last).trim().replaceAll("\\s+", " ");
        }

        if (this.permanentSameAsCurrent && this.currentAddress != null) {
            if (this.permanentAddress == null) {
                this.permanentAddress = new Address();
            }
            this.permanentAddress.setAddressLine1(this.currentAddress.getAddressLine1());
            this.permanentAddress.setAddressLine2(this.currentAddress.getAddressLine2());
            this.permanentAddress.setCity(this.currentAddress.getCity());
            this.permanentAddress.setState(this.currentAddress.getState());
            this.permanentAddress.setPincode(this.currentAddress.getPincode());
        }
    }
}
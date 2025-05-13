package com.wfm.experts.entity.tenant.common;

import com.wfm.experts.entity.tenant.common.enums.BloodGroup;
import com.wfm.experts.entity.tenant.common.enums.Gender;
import com.wfm.experts.entity.tenant.common.enums.MaritalStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "employee_personal_info") // This will be its own table
public class PersonalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Details
    @NotBlank(message = "First Name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @NotBlank(message = "Last Name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "full_name") // Derived
    private String fullName;

    @Column(name = "display_name") // Derived or user-defined
    private String displayName;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @NotNull(message = "Date of Birth is required")
    @Past(message = "Date of Birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group")
    private BloodGroup bloodGroup;

    @NotNull(message = "Marital Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false)
    private MaritalStatus maritalStatus;

    @NotBlank(message = "PAN Number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN number format")
    @Column(name = "pan_number", unique = true, nullable = false)
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$", message = "Invalid Aadhaar number format")
    @Column(name = "aadhaar_number", unique = true, nullable = false)
    private String aadhaarNumber;

    @NotBlank(message = "Nationality is required")
    @Column(name = "nationality", nullable = false)
    private String nationality;

    // Contact Information
    @jakarta.validation.constraints.Email(message = "Invalid personal email format")
    @Column(name = "personal_email", unique = true)
    private String personalEmail;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid alternate mobile number format")
    @Column(name = "alternate_mobile")
    private String alternateMobile;

    @Embedded // EmergencyContact can remain embedded within PersonalInfoEntity
    @Valid
    private EmergencyContact emergencyContact; // Assuming EmergencyContact.java is an @Embeddable

    @Embedded // Address can remain embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine1", column = @Column(name = "current_address_line_1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "current_address_line_2")),
            @AttributeOverride(name = "state", column = @Column(name = "current_state")),
            @AttributeOverride(name = "city", column = @Column(name = "current_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "current_pincode"))
    })
    private Address currentAddress; // Assuming Address.java is an @Embeddable

    @Column(name = "is_permanent_same_as_current")
    private boolean permanentSameAsCurrent = false;

    @Embedded // Address can remain embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine1", column = @Column(name = "permanent_address_line_1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "permanent_address_line_2")),
            @AttributeOverride(name = "state", column = @Column(name = "permanent_state")),
            @AttributeOverride(name = "city", column = @Column(name = "permanent_city")),
            @AttributeOverride(name = "pincode", column = @Column(name = "permanent_pincode"))
    })
    private Address permanentAddress; // Assuming Address.java is an @Embeddable

    // NO Employee employee field for unidirectional mapping from Employee's perspective

    // Lifecycle Callbacks for derived fields
    @PrePersist
    @PreUpdate
    private void deriveFields() {
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

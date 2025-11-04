package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.BloodGroup;
import com.wfm.experts.tenant.common.employees.enums.Gender;
import com.wfm.experts.tenant.common.employees.enums.MaritalStatus;
import com.wfm.experts.validation.groups.OnEmployeeProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfoDTO {

    private Long id;

    @NotBlank(message = "First Name is required", groups = {Default.class, OnEmployeeProfile.class})
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last Name is required", groups = {Default.class, OnEmployeeProfile.class})
    private String lastName;

    private String fullName;
    private String displayName;

    @NotNull(message = "Gender is required", groups = OnEmployeeProfile.class)
    private Gender gender;

    @NotNull(message = "Date of Birth is required", groups = OnEmployeeProfile.class)
    @Past(message = "Date of Birth must be in the past", groups = OnEmployeeProfile.class)
    private LocalDate dateOfBirth;

    private BloodGroup bloodGroup;

    @NotNull(message = "Marital Status is required", groups = OnEmployeeProfile.class)
    private MaritalStatus maritalStatus;

    @NotBlank(message = "PAN Number is required", groups = OnEmployeeProfile.class)
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN number format", groups = OnEmployeeProfile.class)
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is required", groups = OnEmployeeProfile.class)
    @Pattern(regexp = "^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$", message = "Invalid Aadhaar number format", groups = OnEmployeeProfile.class)
    private String aadhaarNumber;

    @NotBlank(message = "Nationality is required", groups = OnEmployeeProfile.class)
    private String nationality;

    @Email(message = "Invalid personal email format", groups = OnEmployeeProfile.class)
    private String personalEmail;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid alternate mobile number format")
    private String alternateMobile;

    @Valid
    private EmergencyContactDTO emergencyContact;

    @Valid
    private AddressDTO currentAddress;

    private boolean permanentSameAsCurrent;

    @Valid
    private AddressDTO permanentAddress;
}
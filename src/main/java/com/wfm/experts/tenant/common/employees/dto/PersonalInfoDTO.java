package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.BloodGroup;
import com.wfm.experts.tenant.common.employees.enums.Gender;
import com.wfm.experts.tenant.common.employees.enums.MaritalStatus;
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
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String displayName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private BloodGroup bloodGroup;
    private MaritalStatus maritalStatus;
    private String panNumber;
    private String aadhaarNumber;
    private String nationality;
    private String personalEmail;
    private String alternateMobile;
    private EmergencyContactDTO emergencyContact;
    private AddressDTO currentAddress;
    private boolean permanentSameAsCurrent;
    private AddressDTO permanentAddress;
}
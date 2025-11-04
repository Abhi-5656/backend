package com.wfm.experts.tenant.common.employees.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.validation.groups.OnEmployeeProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "Employee Code (ID) is required", groups = {Default.class, OnEmployeeProfile.class})
    @Length(max = 50, message = "Employee ID must not exceed 50 characters", groups = {Default.class, OnEmployeeProfile.class})
    private String employeeId;

    @NotBlank(message = "Work Email ID is required", groups = {Default.class, OnEmployeeProfile.class})
    @Email(message = "Invalid email format", groups = {Default.class, OnEmployeeProfile.class})
    private String email;

    @NotBlank(message = "Primary Mobile Number is required", groups = {Default.class, OnEmployeeProfile.class})
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid mobile number format for primary mobile", groups = {Default.class, OnEmployeeProfile.class})
    private String phoneNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotEmpty(message = "System Role is required", groups = {Default.class, OnEmployeeProfile.class})
    private List<String> roles;

    @NotNull(message = "Personal information is required", groups = {Default.class, OnEmployeeProfile.class})
    @Valid // Cascade validation
    private PersonalInfoDTO personalInfo;

    @NotNull(message = "Organizational information is required", groups = {Default.class, OnEmployeeProfile.class})
    @Valid // Cascade validation
    private OrganizationalInfoDTO organizationalInfo;

    @NotNull(message = "Work Location is required for a full profile", groups = OnEmployeeProfile.class)
    private LocationDto workLocation;

    @NotNull(message = "Business Unit is required for a full profile", groups = OnEmployeeProfile.class)
    private BusinessUnitDto businessUnit;

    @NotNull(message = "Designation (Job Title) is required for a full profile", groups = OnEmployeeProfile.class)
    private JobTitleDto jobTitle;

    private String reportingManagerId;
    private String hrManagerId;

    private Date createdAt;
    private Date updatedAt;
}
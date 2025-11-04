package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.EmploymentStatus;
import com.wfm.experts.tenant.common.employees.enums.EmploymentType;
import com.wfm.experts.tenant.common.employees.enums.WorkMode;
import com.wfm.experts.validation.groups.OnEmployeeProfile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class EmploymentDetailsDTO {

    private Long id;

    @NotNull(message = "Date of Joining is required", groups = {Default.class, OnEmployeeProfile.class})
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment Type is required", groups = {Default.class, OnEmployeeProfile.class})
    private EmploymentType employmentType;

    @NotNull(message = "Employment Status is required", groups = {Default.class, OnEmployeeProfile.class})
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Notice Period (Days) is required", groups = {Default.class, OnEmployeeProfile.class})
    @Min(value = 0, message = "Notice period cannot be negative", groups = {Default.class, OnEmployeeProfile.class})
    private Integer noticePeriodDays;

    @NotNull(message = "Work Mode is required", groups = {Default.class, OnEmployeeProfile.class})
    private WorkMode workMode;

    private LocalDate confirmationDate;

    @Min(value = 0, message = "Probation period cannot be negative", groups = OnEmployeeProfile.class)
    private Integer probationPeriodMonths;
}
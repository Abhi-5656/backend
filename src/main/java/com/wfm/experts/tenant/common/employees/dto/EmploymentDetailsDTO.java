package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.EmploymentStatus;
import com.wfm.experts.tenant.common.employees.enums.EmploymentType;
import com.wfm.experts.tenant.common.employees.enums.WorkMode;
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
    private LocalDate dateOfJoining;
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private Integer noticePeriodDays;
    private WorkMode workMode;
    private LocalDate confirmationDate;
    private Integer probationPeriodMonths;
}
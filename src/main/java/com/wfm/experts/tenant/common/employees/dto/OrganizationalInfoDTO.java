package com.wfm.experts.tenant.common.employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationalInfoDTO {

    private Long id;
    private EmploymentDetailsDTO employmentDetails;
    private JobContextDetailsDTO jobContextDetails;
    private LocalDate orgAssignmentEffectiveDate;
}
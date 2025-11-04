package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.validation.groups.OnEmployeeProfile;
import jakarta.validation.Valid;
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
public class OrganizationalInfoDTO {

    private Long id;

    @NotNull(message = "Employment details are required", groups = {Default.class, OnEmployeeProfile.class})
    @Valid
    private EmploymentDetailsDTO employmentDetails;

    @NotNull(message = "Job context details are required", groups = {Default.class, OnEmployeeProfile.class})
    @Valid
    private JobContextDetailsDTO jobContextDetails;

    @NotNull(message = "Organizational assignment effective date is required", groups = {Default.class, OnEmployeeProfile.class})
    private LocalDate orgAssignmentEffectiveDate;
}
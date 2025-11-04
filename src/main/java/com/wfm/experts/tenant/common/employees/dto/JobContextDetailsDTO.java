package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.validation.groups.OnEmployeeProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobContextDetailsDTO {

    private Long id;

    @NotBlank(message = "Department is required", groups = {Default.class, OnEmployeeProfile.class})
    private String departmentName;

    @NotBlank(message = "Job Grade/Band is required", groups = {Default.class, OnEmployeeProfile.class})
    private String jobGradeBand;

    @NotBlank(message = "Cost Center is required", groups = {Default.class, OnEmployeeProfile.class})
    private String costCenter;

    private String organizationalRoleDescription;
}
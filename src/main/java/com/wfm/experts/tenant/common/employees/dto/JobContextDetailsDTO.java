package com.wfm.experts.tenant.common.employees.dto;

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
    private String departmentName;
    private String jobGradeBand;
    private String costCenter;
    private String organizationalRoleDescription;
}
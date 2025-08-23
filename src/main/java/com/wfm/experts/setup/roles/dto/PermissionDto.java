/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDto {

    private Long id;
    private String name;         // e.g. "employee:create"
    private String description;  // human-readable description
    private String moduleName;   // e.g. "HR", "WFM", "Payroll"
}

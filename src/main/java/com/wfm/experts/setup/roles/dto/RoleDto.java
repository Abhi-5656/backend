/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoleDto {

    private Long id;
    private String roleName;              // e.g. "ADMIN", "MANAGER"
    private Set<PermissionDto> permissions; // role’s permissions
}

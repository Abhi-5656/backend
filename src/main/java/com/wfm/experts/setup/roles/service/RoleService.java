/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service;

import com.wfm.experts.setup.roles.dto.RoleDto;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    RoleDto createRole(RoleDto dto);

    RoleDto updateRole(Long id, RoleDto dto);

    void deleteRole(Long id);

    Optional<RoleDto> getRoleById(Long id);

    Optional<RoleDto> getRoleByName(String roleName);

    List<RoleDto> getAllRoles();

    RoleDto assignPermissionToRole(Long roleId, Long permissionId);

    RoleDto removePermissionFromRole(Long roleId, Long permissionId);
}

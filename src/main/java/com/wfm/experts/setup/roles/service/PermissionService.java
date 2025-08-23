/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service;

import com.wfm.experts.setup.roles.dto.PermissionDto;

import java.util.List;
import java.util.Optional;

public interface PermissionService {

    PermissionDto createPermission(PermissionDto dto);

    PermissionDto updatePermission(Long id, PermissionDto dto);

    void deletePermission(Long id);

    Optional<PermissionDto> getPermissionById(Long id);

    Optional<PermissionDto> getPermissionByName(String name);

    List<PermissionDto> getAllPermissions();
}

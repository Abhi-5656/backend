/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service.impl;

import com.wfm.experts.setup.roles.dto.RoleDto;
import com.wfm.experts.setup.roles.entity.Permission;
import com.wfm.experts.setup.roles.entity.Role;
import com.wfm.experts.setup.roles.mapper.RoleMapper;
import com.wfm.experts.setup.roles.repository.PermissionRepository;
import com.wfm.experts.setup.roles.repository.RoleRepository;
import com.wfm.experts.setup.roles.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleDto createRole(RoleDto dto) {
        Role entity = roleMapper.toEntity(dto);
        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto dto) {
        return roleRepository.findById(id)
                .map(existing -> {
                    existing.setRoleName(dto.getRoleName());
                    return roleMapper.toDto(roleRepository.save(existing));
                })
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    public Optional<RoleDto> getRoleById(Long id) {
        return roleRepository.findById(id).map(roleMapper::toDto);
    }

    @Override
    public Optional<RoleDto> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).map(roleMapper::toDto);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleMapper.toDtoList(roleRepository.findAll());
    }

    @Override
    public RoleDto assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        role.getPermissions().add(permission);
        return roleMapper.toDto(roleRepository.save(role));
    }

    @Override
    public RoleDto removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        role.getPermissions().remove(permission);
        return roleMapper.toDto(roleRepository.save(role));
    }
}

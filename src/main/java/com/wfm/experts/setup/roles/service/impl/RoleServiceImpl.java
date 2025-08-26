/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service.impl;

import com.wfm.experts.setup.roles.dto.PermissionDto;
import com.wfm.experts.setup.roles.dto.RoleDto;
import com.wfm.experts.setup.roles.entity.Permission;
import com.wfm.experts.setup.roles.entity.Role;
import com.wfm.experts.setup.roles.mapper.RoleMapper;
import com.wfm.experts.setup.roles.repository.PermissionRepository;
import com.wfm.experts.setup.roles.repository.RoleRepository;
import com.wfm.experts.setup.roles.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleDto createRole(RoleDto dto) {
        Role entity = roleMapper.toEntity(dto);
        entity.setPermissions(Collections.emptySet()); // Initialize with an empty set

        if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
            Set<Long> permissionIds = dto.getPermissions().stream()
                    .map(PermissionDto::getId)
                    .collect(Collectors.toSet());

            List<Permission> managedPermissions = permissionRepository.findAllById(permissionIds);
            // Convert the returned List to a Set before assigning
            entity.setPermissions(new HashSet<>(managedPermissions));
        }

        Role savedEntity = roleRepository.save(entity);
        return roleMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public RoleDto updateRole(Long id, RoleDto dto) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    existingRole.setRoleName(dto.getRoleName());

                    // Clear existing permissions
                    existingRole.getPermissions().clear();

                    if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
                        Set<Long> permissionIds = dto.getPermissions().stream()
                                .map(PermissionDto::getId)
                                .collect(Collectors.toSet());

                        List<Permission> managedPermissions = permissionRepository.findAllById(permissionIds);

                        // addAll efficiently populates the Set from the List
                        existingRole.getPermissions().addAll(managedPermissions);
                    }

                    Role updatedRole = roleRepository.save(existingRole);
                    return roleMapper.toDto(updatedRole);
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
    @Transactional
    public RoleDto assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        // The add method on a Set naturally handles duplicates
        role.getPermissions().add(permission);
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional
    public RoleDto removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));
        return roleMapper.toDto(role);
    }
}
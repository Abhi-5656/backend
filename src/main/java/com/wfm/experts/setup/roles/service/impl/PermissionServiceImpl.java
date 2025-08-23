/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service.impl;

import com.wfm.experts.setup.roles.dto.PermissionDto;
import com.wfm.experts.setup.roles.entity.Permission;
import com.wfm.experts.setup.roles.mapper.PermissionMapper;
import com.wfm.experts.setup.roles.repository.PermissionRepository;
import com.wfm.experts.setup.roles.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionDto createPermission(PermissionDto dto) {
        Permission entity = permissionMapper.toEntity(dto);
        return permissionMapper.toDto(permissionRepository.save(entity));
    }

    @Override
    public PermissionDto updatePermission(Long id, PermissionDto dto) {
        return permissionRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setModuleName(dto.getModuleName());
                    return permissionMapper.toDto(permissionRepository.save(existing));
                })
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    @Override
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission not found with id: " + id);
        }
        permissionRepository.deleteById(id);
    }

    @Override
    public Optional<PermissionDto> getPermissionById(Long id) {
        return permissionRepository.findById(id).map(permissionMapper::toDto);
    }

    @Override
    public Optional<PermissionDto> getPermissionByName(String name) {
        return permissionRepository.findByName(name).map(permissionMapper::toDto);
    }

    @Override
    public List<PermissionDto> getAllPermissions() {
        return permissionMapper.toDtoList(permissionRepository.findAll());
    }
}

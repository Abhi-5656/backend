/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.service.impl;

import com.wfm.experts.setup.roles.dto.PermissionDto;
import com.wfm.experts.setup.roles.entity.Permission;
import com.wfm.experts.setup.roles.mapper.PermissionMapper;
import com.wfm.experts.setup.roles.repository.PermissionRepository;
import com.wfm.experts.setup.roles.service.PermissionService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public List<PermissionDto> createPermissions(List<PermissionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return List.of();

        List<PermissionDto> created = new java.util.ArrayList<>(dtos.size());
        for (PermissionDto dto : dtos) {
            // Create-only behavior: skip if a permission with same name already exists
            if (dto.getName() == null || dto.getName().isBlank()) {
                continue; // or throw an IllegalArgumentException if you want strict validation
            }
            if (permissionRepository.existsByName(dto.getName())) {
                continue; // ignore duplicates by 'name'
            }
            Permission saved = permissionRepository.save(permissionMapper.toEntity(dto));
            created.add(permissionMapper.toDto(saved));
        }
        return created;
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

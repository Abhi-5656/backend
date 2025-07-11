//package com.wfm.experts.setup.roles.service.impl;
//
//import com.wfm.experts.setup.roles.dto.PermissionDTO;
//import com.wfm.experts.setup.roles.entity.Permission;
//import com.wfm.experts.setup.roles.mapper.PermissionMapper;
//import com.wfm.experts.setup.roles.repository.PermissionRepository;
//import com.wfm.experts.setup.roles.service.PermissionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PermissionServiceImpl implements PermissionService {
//
//    private final PermissionRepository permissionRepository;
//    private final PermissionMapper permissionMapper;
//
//    @Override
//    public PermissionDTO createPermission(PermissionDTO dto) {
//        Permission entity = permissionMapper.toEntity(dto);
//        return permissionMapper.toDto(permissionRepository.save(entity));
//    }
//
//    @Override
//    public PermissionDTO updatePermission(Long id, PermissionDTO dto) {
//        Permission existing = permissionRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Permission not found with id " + id));
//
//        existing.setPermissionName(dto.getPermissionName());
//        existing.setDescription(dto.getDescription());
//
//        return permissionMapper.toDto(permissionRepository.save(existing));
//    }
//
//    @Override
//    public void deletePermission(Long id) {
//        permissionRepository.deleteById(id);
//    }
//
//    @Override
//    public PermissionDTO getPermissionById(Long id) {
//        return permissionRepository.findById(id)
//                .map(permissionMapper::toDto)
//                .orElseThrow(() -> new RuntimeException("Permission not found with id " + id));
//    }
//
//    @Override
//    public List<PermissionDTO> getAllPermissions() {
//        return permissionRepository.findAll()
//                .stream()
//                .map(permissionMapper::toDto)
//                .collect(Collectors.toList());
//    }
//}

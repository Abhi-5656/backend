//package com.wfm.experts.setup.roles.service.impl;
//
//import com.wfm.experts.setup.roles.dto.RoleDTO;
//import com.wfm.experts.setup.roles.entity.Role;
//import com.wfm.experts.setup.roles.mapper.RoleMapper;
//import com.wfm.experts.setup.roles.repository.RoleRepository;
//import com.wfm.experts.setup.roles.service.RoleService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class RoleServiceImpl implements RoleService {
//
//    private final RoleRepository roleRepository;
//    private final RoleMapper roleMapper;
//
//    @Override
//    public RoleDTO createRole(RoleDTO roleDTO) {
//        Role role = roleMapper.toEntity(roleDTO);
//        return roleMapper.toDto(roleRepository.save(role));
//    }
//
//    @Override
//    public RoleDTO updateRole(Long roleId, RoleDTO roleDTO) {
//        Role existing = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with id " + roleId));
//
//        existing.setRoleName(roleDTO.getRoleName());
//        existing.setPermissions(roleMapper.toEntity(roleDTO).getPermissions()); // re-map permissions
//        return roleMapper.toDto(roleRepository.save(existing));
//    }
//
//    @Override
//    public void deleteRole(Long roleId) {
//        roleRepository.deleteById(roleId);
//    }
//
//    @Override
//    public RoleDTO getRoleById(Long roleId) {
//        return roleRepository.findById(roleId)
//                .map(roleMapper::toDto)
//                .orElseThrow(() -> new RuntimeException("Role not found with id " + roleId));
//    }
//
//    @Override
//    public List<RoleDTO> getAllRoles() {
//        return roleRepository.findAll()
//                .stream()
//                .map(roleMapper::toDto)
//                .collect(Collectors.toList());
//    }
//}

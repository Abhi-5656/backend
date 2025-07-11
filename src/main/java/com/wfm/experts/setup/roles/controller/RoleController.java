//package com.wfm.experts.setup.roles.controller;
//
//import com.wfm.experts.setup.roles.dto.RoleDTO;
//import com.wfm.experts.setup.roles.service.RoleService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/setup/wfm/roles")
//@RequiredArgsConstructor
//public class RoleController {
//
//    private final RoleService roleService;
//
//    @PostMapping
//    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
//        return ResponseEntity.ok(roleService.createRole(roleDTO));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
//        return ResponseEntity.ok(roleService.updateRole(id, roleDTO));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
//        roleService.deleteRole(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
//        return ResponseEntity.ok(roleService.getRoleById(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<RoleDTO>> getAllRoles() {
//        return ResponseEntity.ok(roleService.getAllRoles());
//    }
//}

/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */
package com.wfm.experts.setup.roles.controller;

import com.wfm.experts.setup.roles.dto.PermissionDto;
import com.wfm.experts.setup.roles.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/setup/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionDto> create(@RequestBody PermissionDto dto) {
        PermissionDto created = permissionService.createPermission(dto);
        return ResponseEntity.status(201).body(created); // return created DTO
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionDto> update(@PathVariable Long id, @RequestBody PermissionDto dto) {
        return ResponseEntity.ok(permissionService.updatePermission(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionDto> getById(@PathVariable Long id) {
        return permissionService.getPermissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PermissionDto>> getAll() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}

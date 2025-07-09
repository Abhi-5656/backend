// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/controller/LeavePolicyController.java
package com.wfm.experts.setup.wfm.leavepolicy.controller;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDTO;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService service;

    /** Create a new policy (with any nested conditionalRules). */
    @PostMapping
    public ResponseEntity<LeavePolicyDTO> create(@Validated @RequestBody LeavePolicyDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    /** Get a policy by its numeric ID. */
    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicyDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** Get a policy by its unique code. */
    @GetMapping("/code/{code}")
    public ResponseEntity<LeavePolicyDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    /** List all policies. */
    @GetMapping
    public ResponseEntity<List<LeavePolicyDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** Update an existing policy (including adds/edits/removals of its conditionalRules). */
    @PutMapping("/{id}")
    public ResponseEntity<LeavePolicyDTO> update(
            @PathVariable Long id,
            @Validated @RequestBody LeavePolicyDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /** Delete a policy (cascades and removes its conditionalRules). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

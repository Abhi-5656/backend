// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/controller/LeaveProfileController.java
package com.wfm.experts.setup.wfm.leavepolicy.controller;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDTO;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/leave-profiles")
@RequiredArgsConstructor
public class LeaveProfileController {

    private final LeaveProfileService service;

    /** Create a new leave‑profile (associating leave policies by ID). */
    @PostMapping
    public ResponseEntity<LeaveProfileDTO> create(
            @Validated @RequestBody LeaveProfileDTO dto) {
        LeaveProfileDTO created = service.createLeaveProfile(dto);
        return ResponseEntity.ok(created);
    }

    /** Get a leave‑profile by its numeric ID. */
    @GetMapping("/{id}")
    public ResponseEntity<LeaveProfileDTO> getById(@PathVariable Long id) {
        LeaveProfileDTO dto = service.getLeaveProfileById(id);
        return ResponseEntity.ok(dto);
    }

    /** List all leave‑profiles. */
    @GetMapping
    public ResponseEntity<List<LeaveProfileDTO>> getAll() {
        List<LeaveProfileDTO> list = service.getAllLeaveProfiles();
        return ResponseEntity.ok(list);
    }

    /** Update an existing leave‑profile (name and policy IDs). */
    @PutMapping("/{id}")
    public ResponseEntity<LeaveProfileDTO> update(
            @PathVariable Long id,
            @Validated @RequestBody LeaveProfileDTO dto) {
        LeaveProfileDTO updated = service.updateLeaveProfile(id, dto);
        return ResponseEntity.ok(updated);
    }

    /** Delete a leave‑profile (by ID). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteLeaveProfile(id);
        return ResponseEntity.noContent().build();
    }
}

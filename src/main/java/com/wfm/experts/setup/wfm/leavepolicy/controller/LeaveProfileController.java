package com.wfm.experts.setup.wfm.leavepolicy.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/leave-profiles")
@RequiredArgsConstructor
public class LeaveProfileController extends WfmSetupController {

    private final LeaveProfileService leaveProfileService;

    @PostMapping
//    @PreAuthorize("hasAuthority('wfm:setup:leave-profile:create')")
    public ResponseEntity<LeaveProfileDto> create(@Valid @RequestBody LeaveProfileDto dto) {
        return new ResponseEntity<>(leaveProfileService.createLeaveProfile(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:leave-profile:update')")
    public ResponseEntity<LeaveProfileDto> update(@PathVariable Long id, @Valid @RequestBody LeaveProfileDto dto) {
        return ResponseEntity.ok(leaveProfileService.updateLeaveProfile(id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:leave-profile:read')")
    public ResponseEntity<LeaveProfileDto> getById(@PathVariable Long id) {
        return leaveProfileService.getLeaveProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:setup:leave-profile:read')")
    public ResponseEntity<List<LeaveProfileDto>> getAll() {
        return ResponseEntity.ok(leaveProfileService.getAllLeaveProfiles());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:leave-profile:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveProfileService.deleteLeaveProfile(id);
        return ResponseEntity.noContent().build();
    }
}
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-profile-assignments")
@RequiredArgsConstructor
@Validated // Enable validation for this controller
public class LeaveProfileAssignmentController {

    private final LeaveProfileAssignmentService assignmentService;

    @PostMapping
//    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:assign')")
    public ResponseEntity<List<LeaveProfileAssignmentDTO>> assignLeaveProfile(@Valid @RequestBody LeaveProfileAssignmentDTO dto) { // Add @Valid
        return ResponseEntity.ok(assignmentService.assignLeaveProfile(dto));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:read')")
    public ResponseEntity<List<LeaveProfileAssignmentDTO>> getAssignmentsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByEmployeeId(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:read')")
    public ResponseEntity<List<LeaveProfileAssignmentDTO>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    /**
     * NEW: Explicitly set an expiration date for an assignment.
     */
    @PutMapping("/{id}/expire")
    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:update')") // Assuming update permission
    public ResponseEntity<LeaveProfileAssignmentDTO> expireAssignment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {

        LeaveProfileAssignmentDTO updatedDto = assignmentService.expireAssignment(id, expirationDate);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * Deactivate an assignment by its ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:delete')") // Assuming delete permission
    public ResponseEntity<Void> deactivateAssignment(@PathVariable Long id) {
        assignmentService.deactivateAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
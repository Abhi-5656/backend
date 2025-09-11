package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-profile-assignments")
@RequiredArgsConstructor
public class LeaveProfileAssignmentController {

    private final LeaveProfileAssignmentService assignmentService;

    @PostMapping
//    @PreAuthorize("hasAuthority('wfm:employee:leave-profile-assignment:assign')")
    public ResponseEntity<List<LeaveProfileAssignmentDTO>> assignLeaveProfile(@RequestBody LeaveProfileAssignmentDTO dto) {
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
}
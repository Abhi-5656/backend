package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto.RequestTypeProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service.RequestTypeProfileAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/request-type-profile-assignments")
@RequiredArgsConstructor
public class RequestTypeProfileAssignmentController {

    private final RequestTypeProfileAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('wfm:employee:request-type-profile-assignment:assign')")
    public ResponseEntity<List<RequestTypeProfileAssignmentDTO>> assignRequestTypeProfile(@RequestBody RequestTypeProfileAssignmentDTO dto) {
        return ResponseEntity.ok(assignmentService.assignRequestTypeProfile(dto));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:request-type-profile-assignment:read')")
    public ResponseEntity<List<RequestTypeProfileAssignmentDTO>> getAssignmentsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByEmployeeId(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:request-type-profile-assignment:read')")
    public ResponseEntity<List<RequestTypeProfileAssignmentDTO>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }
}
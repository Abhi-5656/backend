package com.wfm.experts.modules.wfm.employee.assignment.requesttype.controller;

import com.wfm.experts.modules.wfm.employee.assignment.requesttype.dto.RequestTypeAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.service.RequestTypeAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/request-type-assignments")
@RequiredArgsConstructor
public class RequestTypeAssignmentController {

    private final RequestTypeAssignmentService assignmentService;

    @PostMapping
//    @PreAuthorize("hasAuthority('wfm:employee:request-type-assignment:assign')")
    public ResponseEntity<List<RequestTypeAssignmentDTO>> assignRequestType(@RequestBody RequestTypeAssignmentDTO dto) {
        return ResponseEntity.ok(assignmentService.assignRequestType(dto));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:request-type-assignment:read')")
    public ResponseEntity<List<RequestTypeAssignmentDTO>> getAssignmentsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByEmployeeId(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:request-type-assignment:read')")
    public ResponseEntity<List<RequestTypeAssignmentDTO>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }
}
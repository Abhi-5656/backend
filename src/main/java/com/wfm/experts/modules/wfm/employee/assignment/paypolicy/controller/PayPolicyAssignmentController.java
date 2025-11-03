package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.controller;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception.PayPolicyAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/pay-policy-assignments")
@RequiredArgsConstructor
@Validated // Enable validation for this controller
public class PayPolicyAssignmentController {

    private final PayPolicyAssignmentService payPolicyAssignmentService;

    /**
     * Assign a pay policy to multiple employees.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('wfm:employee:pay-policy-assignment:assign')")
    public ResponseEntity<List<PayPolicyAssignmentDTO>> assignPayPolicy(
            @Valid @RequestBody PayPolicyAssignmentDTO dto) { // Added @Valid
        List<PayPolicyAssignmentDTO> assignedList = payPolicyAssignmentService.assignPayPolicy(dto);
        return ResponseEntity.ok(assignedList);
    }

    /**
     * Get all assignments (admin/debug).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:pay-policy-assignment:read')")
    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAllAssignments() {
        List<PayPolicyAssignmentDTO> all = payPolicyAssignmentService.getAllAssignments();
        return ResponseEntity.ok(all);
    }

    /**
     * Get all assignments for a specific employee.
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:pay-policy-assignment:read') or (hasAuthority('wfm:employee:pay-policy-assignment:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAssignmentsByEmployee(
            @PathVariable String employeeId) {
        List<PayPolicyAssignmentDTO> list = payPolicyAssignmentService.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get current assignment for an employee (effective on a given date).
     */
    @GetMapping("/employee/{employeeId}/current")
    @PreAuthorize("hasAuthority('wfm:employee:pay-policy-assignment:read') or (hasAuthority('wfm:employee:pay-policy-assignment:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<PayPolicyAssignmentDTO> getCurrentAssignment(
            @PathVariable String employeeId
    ) {
        PayPolicyAssignmentDTO current = payPolicyAssignmentService.getCurrentAssignment(employeeId);
        if (current == null) {
            // Throw exception instead of returning ResponseEntity.notFound()
            throw new PayPolicyAssignmentResourceNotFoundException(
                    "No current pay policy assignment found for employee: " + employeeId
            );
        }
        return ResponseEntity.ok(current);
    }
}
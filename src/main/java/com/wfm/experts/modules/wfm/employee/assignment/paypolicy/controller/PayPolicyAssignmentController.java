package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.controller;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/pay-policy-assignments")
@RequiredArgsConstructor
public class PayPolicyAssignmentController {

    private final PayPolicyAssignmentService payPolicyAssignmentService;

    /**
     * Assign a pay policy to an employee.
     */
    @PostMapping
    public ResponseEntity<PayPolicyAssignmentDTO> assignPayPolicy(
            @RequestBody PayPolicyAssignmentDTO dto) {
        PayPolicyAssignmentDTO assigned = payPolicyAssignmentService.assignPayPolicy(dto);
        return ResponseEntity.ok(assigned);
    }

    /**
     * Get all assignments (admin/debug).
     */
    @GetMapping
    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAllAssignments() {
        List<PayPolicyAssignmentDTO> all = payPolicyAssignmentService.getAllAssignments();
        return ResponseEntity.ok(all);
    }

    /**
     * Get all assignments for a specific employee.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAssignmentsByEmployee(
            @PathVariable String employeeId) {
        List<PayPolicyAssignmentDTO> list = payPolicyAssignmentService.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get current assignment for an employee (effective on a given date).
     */
    @GetMapping("/employee/{employeeId}/current")
    public ResponseEntity<PayPolicyAssignmentDTO> getCurrentAssignment(
            @PathVariable String employeeId,
            @RequestParam("date") String dateStr // YYYY-MM-DD
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        // Assuming "current" means effectiveDate <= date and expirationDate >= date
        PayPolicyAssignmentDTO current = payPolicyAssignmentService.getCurrentAssignment(employeeId, date, date);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(current);
    }
}

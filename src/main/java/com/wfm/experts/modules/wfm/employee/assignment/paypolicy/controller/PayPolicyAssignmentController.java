//package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.controller;
//
//import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
//import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/employee/pay-policy-assignments")
//@RequiredArgsConstructor
//public class PayPolicyAssignmentController {
//
//    private final PayPolicyAssignmentService payPolicyAssignmentService;
//
//    /**
//     * Assign a pay policy to multiple employees.
//     */
//    @PostMapping
//    public ResponseEntity<List<PayPolicyAssignmentDTO>> assignPayPolicy(
//            @RequestBody PayPolicyAssignmentDTO dto) {
//        List<PayPolicyAssignmentDTO> assignedList = payPolicyAssignmentService.assignPayPolicy(dto);
//        return ResponseEntity.ok(assignedList);
//    }
//
//    /**
//     * Get all assignments (admin/debug).
//     */
//    @GetMapping
//    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAllAssignments() {
//        List<PayPolicyAssignmentDTO> all = payPolicyAssignmentService.getAllAssignments();
//        return ResponseEntity.ok(all);
//    }
//
//    /**
//     * Get all assignments for a specific employee.
//     */
//    @GetMapping("/employee/{employeeId}")
//    public ResponseEntity<List<PayPolicyAssignmentDTO>> getAssignmentsByEmployee(
//            @PathVariable String employeeId) {
//        List<PayPolicyAssignmentDTO> list = payPolicyAssignmentService.getAssignmentsByEmployeeId(employeeId);
//        return ResponseEntity.ok(list);
//    }
//
//    /**
//     * Get current assignment for an employee (effective on a given date).
//     */
//    @GetMapping("/employee/{employeeId}/current")
//    public ResponseEntity<PayPolicyAssignmentDTO> getCurrentAssignment(
//            @PathVariable String employeeId
//    ) {
//        PayPolicyAssignmentDTO current = payPolicyAssignmentService.getCurrentAssignment(employeeId);
//        if (current == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(current);
//    }
//
//
//}
package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.controller;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/pay-policy-assignments")
@RequiredArgsConstructor
public class PayPolicyAssignmentController {

    private final PayPolicyAssignmentService payPolicyAssignmentService;

    /**
     * Assign a pay policy to multiple employees.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('wfm:employee:pay-policy-assignment:assign')")
    public ResponseEntity<List<PayPolicyAssignmentDTO>> assignPayPolicy(
            @RequestBody PayPolicyAssignmentDTO dto) {
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
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(current);
    }
}
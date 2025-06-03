package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;

import java.time.LocalDate;
import java.util.List;

public interface PayPolicyAssignmentService {

    /**
     * Assigns a pay policy to an employee.
     * Checks if the employee exists before assignment.
     */
    PayPolicyAssignmentDTO assignPayPolicy(PayPolicyAssignmentDTO dto);

    /**
     * Gets all assignments for a particular employee.
     */
    List<PayPolicyAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);

    /**
     * Gets the current (active) pay policy assignment for an employee, based on date.
     */
    PayPolicyAssignmentDTO getCurrentAssignment(String employeeId, LocalDate effectiveDate, LocalDate expirationDate);

    /**
     * Gets all pay policy assignments in the system.
     */
    List<PayPolicyAssignmentDTO> getAllAssignments();
}

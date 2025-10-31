package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception;

/**
 * Thrown for bad request data in a pay policy assignment (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class PayPolicyAssignmentValidationException extends RuntimeException {
    public PayPolicyAssignmentValidationException(String message) {
        super(message);
    }
}
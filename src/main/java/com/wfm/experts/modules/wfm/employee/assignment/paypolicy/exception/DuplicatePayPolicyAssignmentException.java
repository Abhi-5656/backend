package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception;

/**
 * Thrown when a pay policy assignment conflicts with an existing one.
 * Extends RuntimeException directly.
 */
public class DuplicatePayPolicyAssignmentException extends RuntimeException {
    public DuplicatePayPolicyAssignmentException(String message) {
        super(message);
    }
}
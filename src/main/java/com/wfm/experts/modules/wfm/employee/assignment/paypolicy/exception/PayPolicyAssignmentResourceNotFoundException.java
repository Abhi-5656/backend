package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception;

/**
 * Thrown when a related resource (like Employee or PayPolicy) is not found.
 * Extends RuntimeException directly.
 */
public class PayPolicyAssignmentResourceNotFoundException extends RuntimeException {
    public PayPolicyAssignmentResourceNotFoundException(String message) {
        super(message);
    }
}
package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception;

/**
 * Thrown when a related resource (like Employee or ShiftRotation) is not found.
 * Extends RuntimeException directly.
 */
public class ShiftRotationAssignmentResourceNotFoundException extends RuntimeException {
    public ShiftRotationAssignmentResourceNotFoundException(String message) {
        super(message);
    }
}
package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception;

/**
 * Thrown when a shift rotation assignment conflicts with an existing one (e.g., date overlap).
 * Extends RuntimeException directly.
 */
public class DuplicateShiftRotationAssignmentException extends RuntimeException {
    public DuplicateShiftRotationAssignmentException(String message) {
        super(message);
    }
}
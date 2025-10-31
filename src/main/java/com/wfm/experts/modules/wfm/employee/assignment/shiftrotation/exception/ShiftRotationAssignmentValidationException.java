package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception;

/**
 * Thrown for bad request data in a shift rotation assignment (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class ShiftRotationAssignmentValidationException extends RuntimeException {
    public ShiftRotationAssignmentValidationException(String message) {
        super(message);
    }
}
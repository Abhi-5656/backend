package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception;

/**
 * Thrown for bad request data in a leave profile assignment (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class LeaveProfileAssignmentValidationException extends RuntimeException {
    public LeaveProfileAssignmentValidationException(String message) {
        super(message);
    }
}
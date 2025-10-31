package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception;

/**
 * Thrown when a leave profile assignment conflicts with an existing one (e.g., date overlap).
 * Extends RuntimeException directly.
 */
public class DuplicateLeaveProfileAssignmentException extends RuntimeException {
    public DuplicateLeaveProfileAssignmentException(String message) {
        super(message);
    }
}
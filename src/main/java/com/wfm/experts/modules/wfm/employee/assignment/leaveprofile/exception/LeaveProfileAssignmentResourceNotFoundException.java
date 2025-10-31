package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception;

/**
 * Thrown when a related resource (like Employee or LeaveProfile) is not found.
 * Extends RuntimeException directly.
 */
public class LeaveProfileAssignmentResourceNotFoundException extends RuntimeException {
    public LeaveProfileAssignmentResourceNotFoundException(String message) {
        super(message);
    }
}
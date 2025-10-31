package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception;

/**
 * Thrown when a holiday profile assignment conflicts with an existing one (e.g., date overlap).
 * Extends RuntimeException directly.
 */
public class DuplicateHolidayProfileAssignmentException extends RuntimeException {
    public DuplicateHolidayProfileAssignmentException(String message) {
        super(message);
    }
}
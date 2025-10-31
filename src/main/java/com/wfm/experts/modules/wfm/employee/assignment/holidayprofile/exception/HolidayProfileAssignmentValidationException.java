package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception;

/**
 * Thrown for bad request data in a holiday profile assignment (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class HolidayProfileAssignmentValidationException extends RuntimeException {
    public HolidayProfileAssignmentValidationException(String message) {
        super(message);
    }
}
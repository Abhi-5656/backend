package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception;

/**
 * Thrown when a related resource (like Employee or HolidayProfile) is not found.
 * Extends RuntimeException directly.
 */
public class HolidayProfileAssignmentResourceNotFoundException extends RuntimeException {
    public HolidayProfileAssignmentResourceNotFoundException(String message) {
        super(message);
    }
}
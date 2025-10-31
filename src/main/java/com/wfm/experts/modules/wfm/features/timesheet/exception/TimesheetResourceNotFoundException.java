package com.wfm.experts.modules.wfm.features.timesheet.exception;

/**
 * Thrown when a related resource (like an Employee) is not found during a timesheet operation.
 * Extends RuntimeException directly.
 */
public class TimesheetResourceNotFoundException extends RuntimeException {
    public TimesheetResourceNotFoundException(String message) {
        super(message);
    }
}
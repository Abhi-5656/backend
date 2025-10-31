package com.wfm.experts.modules.wfm.features.timesheet.exception;

/**
 * Thrown for bad request data in timesheet or punch operations (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class TimesheetValidationException extends RuntimeException {
    public TimesheetValidationException(String message) {
        super(message);
    }
}
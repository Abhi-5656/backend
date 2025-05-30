package com.wfm.experts.modules.wfm.features.timesheet.exception;

public class TimesheetNotFoundException extends RuntimeException {
    public TimesheetNotFoundException() {
        super();
    }

    public TimesheetNotFoundException(String message) {
        super(message);
    }

    public TimesheetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

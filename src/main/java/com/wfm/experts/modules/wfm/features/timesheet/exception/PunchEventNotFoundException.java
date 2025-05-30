package com.wfm.experts.modules.wfm.features.timesheet.exception;

public class PunchEventNotFoundException extends RuntimeException {
    public PunchEventNotFoundException() {
        super();
    }

    public PunchEventNotFoundException(String message) {
        super(message);
    }

    public PunchEventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

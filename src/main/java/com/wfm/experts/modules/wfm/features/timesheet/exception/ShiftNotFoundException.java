package com.wfm.experts.modules.wfm.features.timesheet.exception;

public class ShiftNotFoundException extends RuntimeException {
    public ShiftNotFoundException(String message) {
        super(message);
    }
}

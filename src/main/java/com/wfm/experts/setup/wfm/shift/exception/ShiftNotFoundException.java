package com.wfm.experts.setup.wfm.shift.exception;

public class ShiftNotFoundException extends RuntimeException {
    public ShiftNotFoundException(Long id) {
        super("Shift not found with id: " + id);
    }
}

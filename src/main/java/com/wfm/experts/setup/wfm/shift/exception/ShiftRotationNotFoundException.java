package com.wfm.experts.setup.wfm.shift.exception;

public class ShiftRotationNotFoundException extends RuntimeException {
    public ShiftRotationNotFoundException(Long id) {
        super("ShiftRotation not found with id: " + id);
    }
}

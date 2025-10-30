package com.wfm.experts.setup.wfm.shift.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShiftRotationAlreadyExistsException extends RuntimeException {
    public ShiftRotationAlreadyExistsException(String rotationName) {
        super("Shift Rotation with name '" + rotationName + "' already exists.");
    }
}

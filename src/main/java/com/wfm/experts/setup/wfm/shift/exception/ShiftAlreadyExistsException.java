package com.wfm.experts.setup.wfm.shift.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShiftAlreadyExistsException extends RuntimeException {
    public ShiftAlreadyExistsException(String shiftName) {
        super("Shift with name '" + shiftName + "' already exists.");
    }
}
package com.wfm.experts.setup.wfm.holiday.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HolidayAlreadyExistsException extends RuntimeException {
    public HolidayAlreadyExistsException(String message) {
        super(message);
    }
}
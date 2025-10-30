package com.wfm.experts.setup.wfm.holiday.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HolidayProfileAlreadyExistsException extends RuntimeException {
    public HolidayProfileAlreadyExistsException(String message) {
        super(message);
    }
}
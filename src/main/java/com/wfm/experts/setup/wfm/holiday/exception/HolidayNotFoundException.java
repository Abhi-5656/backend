package com.wfm.experts.setup.wfm.holiday.exception;

public class HolidayNotFoundException extends RuntimeException {
    public HolidayNotFoundException(Long id) {
        super("Holiday not found with id: " + id);
    }

    public HolidayNotFoundException(String message) {
        super(message);
    }
}

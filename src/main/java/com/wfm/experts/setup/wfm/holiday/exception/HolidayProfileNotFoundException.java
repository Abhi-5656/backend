package com.wfm.experts.setup.wfm.holiday.exception;

public class HolidayProfileNotFoundException extends RuntimeException {
    public HolidayProfileNotFoundException(Long id) {
        super("Holiday profile not found with id: " + id);
    }
}

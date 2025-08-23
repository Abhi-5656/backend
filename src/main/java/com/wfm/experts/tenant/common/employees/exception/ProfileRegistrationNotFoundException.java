package com.wfm.experts.tenant.common.employees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an EmployeeProfileRegistration record cannot be found.
 * This results in a 404 Not Found HTTP status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileRegistrationNotFoundException extends RuntimeException {

    public ProfileRegistrationNotFoundException(String message) {
        super(message);
    }
}
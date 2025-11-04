package com.wfm.experts.tenant.common.employees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create an employee with a duplicate unique identifier
 * (e.g., email, employeeId, phone number).
 * This results in a 409 Conflict HTTP status.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmployeeAlreadyExistsException extends RuntimeException {

    public EmployeeAlreadyExistsException(String field, String value) {
        super(String.format("Employee with %s '%s' already exists.", field, value));
    }

    public EmployeeAlreadyExistsException(String message) {
        super(message);
    }
}
package com.wfm.experts.tenant.common.employees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A generic exception thrown when a requested resource is not found in the system.
 * This typically results in a 404 Not Found HTTP status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with a custom message.
     *
     * @param message The detail message explaining the error.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * A more structured constructor to create a standardized error message.
     *
     * @param resourceName The name of the resource that was not found (e.g., "Employee", "JobTitle").
     * @param identifier The identifier used to look up the resource (e.g., an ID or email).
     */
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }
}
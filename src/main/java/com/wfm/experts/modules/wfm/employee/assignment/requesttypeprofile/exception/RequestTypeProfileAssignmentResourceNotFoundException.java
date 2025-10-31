package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception;

/**
 * Thrown when a related resource (like Employee or RequestTypeProfile) is not found.
 * Extends RuntimeException directly.
 */
public class RequestTypeProfileAssignmentResourceNotFoundException extends RuntimeException {
    public RequestTypeProfileAssignmentResourceNotFoundException(String message) {
        super(message);
    }
}
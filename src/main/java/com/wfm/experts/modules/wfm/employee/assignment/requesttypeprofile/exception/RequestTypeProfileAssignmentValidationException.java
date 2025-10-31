package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception;

/**
 * Thrown for bad request data in a request type profile assignment (e.g., invalid dates).
 * Extends RuntimeException directly.
 */
public class RequestTypeProfileAssignmentValidationException extends RuntimeException {
    public RequestTypeProfileAssignmentValidationException(String message) {
        super(message);
    }
}
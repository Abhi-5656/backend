package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception;

/**
 * Thrown when a request type profile assignment conflicts with an existing one (e.g., date overlap).
 * Extends RuntimeException directly.
 */
public class DuplicateRequestTypeProfileAssignmentException extends RuntimeException {
    public DuplicateRequestTypeProfileAssignmentException(String message) {
        super(message);
    }
}
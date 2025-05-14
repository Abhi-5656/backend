package com.wfm.experts.notificationengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception to indicate that an authenticated user
 * attempted an unauthorized action on a notification resource
 * (e.g., accessing or modifying a notification not belonging to them).
 *
 * This typically results in an HTTP 403 Forbidden response if it propagates
 * to the controller layer and is not handled by a more specific exception handler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // HTTP 403 Forbidden
public class UnauthorizedNotificationAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedNotificationAccessException with the specified detail message.
     *
     * @param message the detail message.
     */
    public UnauthorizedNotificationAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedNotificationAccessException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A {@code null} value is
     * permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UnauthorizedNotificationAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

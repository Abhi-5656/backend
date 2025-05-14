package com.wfm.experts.notificationengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception to indicate that a requested AppNotification
 * could not be found.
 *
 * This typically results in an HTTP 404 Not Found response if it propagates
 * to the controller layer and is not handled by a more specific exception handler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 Not Found
public class AppNotificationNotFoundException extends RuntimeException {

    /**
     * Constructs a new AppNotificationNotFoundException with the specified detail message.
     *
     * @param message the detail message.
     */
    public AppNotificationNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new AppNotificationNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A {@code null} value is
     * permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AppNotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.wfm.experts.notificationengine.exception;

/**
 * Custom runtime exception to indicate an error occurred during the
 * general processing of a notification within the notification engine.
 *
 * This can be used for errors that are not specific to a particular
 * sending channel (like email or SMS) or a specific integration,
 * but rather to the overall workflow or internal logic of processing
 * a notification request.
 */
public class NotificationProcessingException extends RuntimeException {

    /**
     * Constructs a new NotificationProcessingException with the specified detail message.
     *
     * @param message the detail message.
     */
    public NotificationProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new NotificationProcessingException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A {@code null} value is
     * permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public NotificationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new NotificationProcessingException with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A {@code null} value is
     * permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public NotificationProcessingException(Throwable cause) {
        super(cause);
    }
}

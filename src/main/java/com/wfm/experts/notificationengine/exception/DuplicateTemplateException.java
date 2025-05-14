package com.wfm.experts.notificationengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception to indicate that an attempt was made to create
 * or update a NotificationTemplate that would result in a duplicate entry
 * based on unique constraints (e.g., same templateId, channel, language, and version).
 *
 * This typically results in an HTTP 409 Conflict response.
 */
@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409 Conflict
public class DuplicateTemplateException extends RuntimeException {
    public DuplicateTemplateException(String message) {
        super(message);
    }

    public DuplicateTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

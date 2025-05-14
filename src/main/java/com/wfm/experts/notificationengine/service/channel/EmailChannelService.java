package com.wfm.experts.notificationengine.service.channel;

import com.wfm.experts.notificationengine.dto.NotificationRequest;

/**
 * Service interface for handling the dispatch of email notifications.
 * Implementations of this interface will interact with an actual email sending provider
 * (e.g., SMTP server, SendGrid, AWS SES).
 */
public interface EmailChannelService {

    /**
     * Sends an email based on the details provided in the NotificationRequest.
     * This method will typically:
     * 1. Use a {@link com.wfm.experts.notificationengine.service.TemplatingService}
     * to get the rendered subject and body for the email using the
     * templateId and payload from the notificationRequest.
     * 2. Interact with an email sending client/library to dispatch the email
     * to the recipientAddress specified in the notificationRequest.
     * 3. Handle any exceptions that occur during the email sending process.
     * 4. (Optionally) Update a {@link com.wfm.experts.notificationengine.entity.NotificationLog}
     * with the status from the email provider (e.g., provider's message ID, delivery status if available immediately).
     *
     * @param notificationRequest The DTO containing all necessary details for the email,
     * including recipient, templateId, and payload for template rendering.
     * @throws EmailSendingException if an error occurs specifically during the email dispatch process
     * (e.g., connection failure to SMTP server, API error from email provider).
     */
    void sendEmail(NotificationRequest notificationRequest) throws EmailSendingException;

    /**
     * Custom exception for errors that occur specifically during email dispatch.
     */
    class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message) {
            super(message);
        }

        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

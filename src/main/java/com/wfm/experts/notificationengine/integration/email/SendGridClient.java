package com.wfm.experts.notificationengine.integration.email;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface for a client that interacts with the SendGrid API to send emails.
 * This abstraction allows for different implementations, including a real client
 * using the SendGrid Java library or a mock client for testing.
 */
public interface SendGridClient {

    /**
     * Sends an email using the SendGrid API.
     *
     * @param fromEmail   The email address of the sender.
     * @param fromName    (Optional) The name of the sender.
     * @param toEmail     The email address of the primary recipient.
     * @param toName      (Optional) The name of the primary recipient.
     * @param subject     The subject line of the email.
     * @param htmlContent The HTML body of the email.
     * @param textContent (Optional) The plain text version of the email body, for email clients
     * that do not support HTML or when the user prefers plain text.
     * @param ccEmails    (Optional) A list of email addresses to CC.
     * @param bccEmails   (Optional) A list of email addresses to BCC.
     * @param attachments (Optional) A map where keys are filenames and values are
     * {@link InputStream}s for the attachment content.
     * The InputStream will be read by the implementation.
     * @return A message ID or a unique identifier from SendGrid if the email was
     * successfully accepted for delivery by the SendGrid API.
     * @throws SendGridEmailException if an error occurs while trying to send the email
     * via SendGrid (e.g., API authentication failure,
     * invalid parameters, network issues).
     */
    String sendEmail(
            String fromEmail,
            String fromName,
            String toEmail,
            String toName,
            String subject,
            String htmlContent,
            String textContent,
            List<String> ccEmails,
            List<String> bccEmails,
            Map<String, InputStream> attachments
    ) throws SendGridEmailException;

    // Potential future enhancements for this interface:
    // - Method to send email using a SendGrid Dynamic Template:
    //   sendEmailWithDynamicTemplate(String fromEmail, String toEmail, String templateId, Map<String, Object> dynamicTemplateData)
    // - Methods for batch sending or managing contacts/lists if needed.
    // - Methods to handle more advanced SendGrid features like scheduling, categories, IP pools, etc.

    /**
     * Custom exception for errors that occur specifically during SendGrid email dispatch.
     * This can wrap exceptions from the SendGrid library or represent other API interaction issues.
     */
    class SendGridEmailException extends RuntimeException {
        private Integer statusCode; // Optional: to store HTTP status code from SendGrid API error response

        public SendGridEmailException(String message) {
            super(message);
        }

        public SendGridEmailException(String message, Throwable cause) {
            super(message, cause);
        }

        public SendGridEmailException(String message, Integer statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public SendGridEmailException(String message, Throwable cause, Integer statusCode) {
            super(message, cause);
            this.statusCode = statusCode;
        }

        /**
         * Gets the HTTP status code from the SendGrid API response, if available.
         * @return The HTTP status code, or null if not applicable.
         */
        public Integer getStatusCode() {
            return statusCode;
        }
    }
}

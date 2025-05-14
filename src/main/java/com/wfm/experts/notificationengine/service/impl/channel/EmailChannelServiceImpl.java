package com.wfm.experts.notificationengine.service.impl.channel;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.NotificationLog;
import com.wfm.experts.notificationengine.repository.NotificationLogRepository;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.notificationengine.service.channel.EmailChannelService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of {@link EmailChannelService}.
 * Handles the actual dispatch of email notifications using JavaMailSender.
 */
@Service
public class EmailChannelServiceImpl implements EmailChannelService {

    private static final Logger logger = LoggerFactory.getLogger(EmailChannelServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplatingService templatingService;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${spring.mail.username}") // Assuming this is your 'from' address or configured in mail sender
    private String fromEmailAddress;

    @Value("${notification.email.default-language:en-US}")
    private String defaultEmailLanguage;


    @Autowired
    public EmailChannelServiceImpl(JavaMailSender mailSender,
                                   TemplatingService templatingService,
                                   NotificationLogRepository notificationLogRepository) {
        this.mailSender = mailSender;
        this.templatingService = templatingService;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Sends an email notification.
     * It fetches and renders the template, then sends the email using JavaMailSender.
     * Updates the NotificationLog with the outcome.
     *
     * @param notificationRequest The request DTO containing notification details.
     * @throws EmailSendingException if sending the email fails.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // New transaction for log updates
    public void sendEmail(NotificationRequest notificationRequest) throws EmailSendingException {
        String recipientEmail = notificationRequest.getRecipientAddress();
        String templateId = notificationRequest.getTemplateId();
        String notificationId = notificationRequest.getNotificationId();

        logger.info("Attempting to send email for notification ID: {}, Recipient: {}", notificationId, recipientEmail);

        // Fetch and update the log entry - marking as PROCESSING
        Optional<NotificationLog> logOptional = notificationLogRepository.findByNotificationRequestId(notificationId);
        if (logOptional.isEmpty()) {
            logger.error("NotificationLog not found for request ID: {}. Cannot proceed with email sending.", notificationId);
            // This scenario should ideally not happen if NotificationOrchestrationService creates the log first.
            // Throwing an exception here as it's a break in the expected flow.
            throw new EmailSendingException("NotificationLog not found for request ID: " + notificationId);
        }
        NotificationLog logEntry = logOptional.get();
        updateLogStatus(logEntry, NotificationLog.NotificationStatus.PROCESSING, "Attempting to render and send email.", null);

        try {
            // 1. Get rendered template content
            // Assuming language might be part of metadata or user profile, for now using a default
            String language = notificationRequest.getMetadata() != null ?
                    notificationRequest.getMetadata().getOrDefault("language", defaultEmailLanguage) :
                    defaultEmailLanguage;

            TemplatingService.RenderedTemplateContent renderedContent = templatingService.getAndRenderTemplate(
                    templateId,
                    NotificationRequest.ChannelType.EMAIL, // Explicitly EMAIL
                    language,
                    notificationRequest.getPayload()
            ).orElseThrow(() -> {
                logger.error("Failed to render email template ID: {} for notification ID: {}", templateId, notificationId);
                return new EmailSendingException("Email template rendering failed for template ID: " + templateId);
            });

            String subject = renderedContent.getSubject().orElse("Notification"); // Default subject if not provided by template
            String body = renderedContent.getBody();

            // 2. Construct and send the email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // For HTML emails with potential embedded resources
                    StandardCharsets.UTF_8.name());

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML content
            helper.setFrom(fromEmailAddress); // Set your 'from' address

            // Add attachments or inline resources if needed, based on notificationRequest.getPayload()
            // e.g., if (notificationRequest.getPayload().containsKey("attachmentPath")) { ... helper.addAttachment(...) ... }

            mailSender.send(mimeMessage);
            logger.info("Email sent successfully for notification ID: {} to {}", notificationId, recipientEmail);

            // 3. Update log to SENT
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.SENT, "Email dispatched successfully.", null); // providerMessageId might not be available from JavaMailSender directly

        } catch (TemplatingService.TemplateProcessingException e) {
            logger.error("Template processing error for notification ID: {}: {}", notificationId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Template processing error: " + e.getMessage(), null);
            throw new EmailSendingException("Failed due to template processing error: " + e.getMessage(), e);
        } catch (MailException | MessagingException e) {
            logger.error("Failed to send email for notification ID: {}. Error: {}", notificationId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Email sending failed: " + e.getMessage(), null);
            throw new EmailSendingException("Failed to send email to " + recipientEmail + ": " + e.getMessage(), e);
        } catch (Exception e) { // Catch any other unexpected errors
            logger.error("Unexpected error during email sending for notification ID: {}. Error: {}", notificationId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Unexpected error: " + e.getMessage(), null);
            throw new EmailSendingException("Unexpected error sending email to " + recipientEmail + ": " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to update the NotificationLog status and save it.
     * This runs in its own transaction to ensure log updates are committed
     * even if the outer email sending operation has issues after this point
     * (though in this flow, it's mostly for final status updates).
     */
    private void updateLogStatus(NotificationLog logEntry, NotificationLog.NotificationStatus status, String statusMessage, String providerMessageId) {
        try {
            logEntry.setStatus(status);
            if (statusMessage != null) {
                logEntry.setStatusMessage(statusMessage.substring(0, Math.min(statusMessage.length(), 2000))); // Truncate if too long for DB
            }
            if (providerMessageId != null) {
                logEntry.setProviderMessageId(providerMessageId);
            }

            if (status == NotificationLog.NotificationStatus.PROCESSING) {
                logEntry.setAttemptCount(logEntry.getAttemptCount() + 1);
            } else if (status == NotificationLog.NotificationStatus.SENT) {
                logEntry.setSentAt(LocalDateTime.now());
            } else if (status == NotificationLog.NotificationStatus.FAILED) {
                logEntry.setFailedAt(LocalDateTime.now());
            }
            notificationLogRepository.saveAndFlush(logEntry); // Save immediately
        } catch (Exception e) {
            logger.error("Failed to update notification log for request ID: {}. Status: {}, Error: {}",
                    logEntry.getNotificationRequestId(), status, e.getMessage(), e);
            // This is a secondary failure (logging the primary failure). Log it but don't let it overshadow the original exception.
        }
    }
}

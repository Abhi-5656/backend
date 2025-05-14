package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.NotificationLog;
import com.wfm.experts.notificationengine.producer.NotificationProducer;
import com.wfm.experts.notificationengine.repository.NotificationLogRepository;
import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
// Import NotificationJsonUtil if you were to manually serialize payload/metadata to string for TEXT columns
// import com.wfm.experts.notificationengine.util.NotificationJsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Optional: if logging and producing should be atomic

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementation of {@link NotificationOrchestrationService}.
 * Orchestrates the initial processing of a notification request, including
 * validation, logging, and publishing to the message queue.
 */
@Service
public class NotificationOrchestrationServiceImpl implements NotificationOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationOrchestrationServiceImpl.class);

    private final NotificationProducer notificationProducer;
    private final NotificationLogRepository notificationLogRepository;

    @Autowired
    public NotificationOrchestrationServiceImpl(
            NotificationProducer notificationProducer,
            NotificationLogRepository notificationLogRepository) {
        this.notificationProducer = notificationProducer;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Processes an incoming notification request.
     * Steps:
     * 1. Validates the request.
     * 2. Creates and saves an initial {@link NotificationLog} with PENDING status.
     * 3. Publishes the request to RabbitMQ via {@link NotificationProducer}.
     * 4. If publishing fails, updates the log status to FAILED.
     *
     * @param notificationRequest The DTO containing details of the notification.
     * @throws IllegalArgumentException if the notificationRequest is invalid.
     * @throws RuntimeException if publishing to RabbitMQ fails or other critical errors occur.
     */
    @Override
    @Transactional // Makes the DB save and MQ publish atomic if desired (requires careful thought on distributed transactions if MQ is external)
    // For now, primarily ensures log is saved before attempting to send.
    public void processNotificationRequest(NotificationRequest notificationRequest) {
        validateNotificationRequest(notificationRequest);

        logger.info("Processing notification request ID: {}", notificationRequest.getNotificationId());

        NotificationLog logEntry = createInitialLogEntry(notificationRequest);

        try {
            // Save the initial log entry with PENDING status
            notificationLogRepository.save(logEntry);
            logger.debug("Notification log saved with PENDING status for ID: {}", logEntry.getNotificationRequestId());

            // Send the notification to the message queue
            notificationProducer.sendNotification(notificationRequest);
            logger.info("Notification request ID: {} successfully handed off to producer.", notificationRequest.getNotificationId());

            // Note: The log status will be updated by consumers upon successful sending or definitive failure.
            // Here, we've logged it as PENDING and handed it off.
            // If sendNotification immediately threw an exception (e.g., RabbitMQ down), the catch block below handles it.

        } catch (Exception e) {
            // If sending to RabbitMQ fails critically (e.g., broker down, configuration error)
            logger.error("Critical error publishing notification ID: {} to RabbitMQ. Error: {}",
                    notificationRequest.getNotificationId(), e.getMessage(), e);

            // Update the log entry to FAILED status
            logEntry.setStatus(NotificationLog.NotificationStatus.FAILED);
            logEntry.setStatusMessage("Failed to publish to message queue: " + e.getMessage());
            logEntry.setFailedAt(LocalDateTime.now());
            notificationLogRepository.save(logEntry); // Attempt to save the updated FAILED status

            // Re-throw a custom exception or a more generic runtime exception
            // to signal the failure to the caller (e.g., the controller).
            throw new RuntimeException("Failed to process notification request due to messaging system error for ID: " +
                    notificationRequest.getNotificationId(), e);
        }
    }

    /**
     * Validates the incoming {@link NotificationRequest}.
     *
     * @param request The notification request to validate.
     * @throws IllegalArgumentException if validation fails.
     */
    private void validateNotificationRequest(NotificationRequest request) {
        Objects.requireNonNull(request, "NotificationRequest cannot be null.");
        Objects.requireNonNull(request.getNotificationId(), "Notification ID cannot be null or empty.");
        if(request.getNotificationId().trim().isEmpty()){
            throw new IllegalArgumentException("Notification ID cannot be empty.");
        }
        Objects.requireNonNull(request.getChannel(), "Notification channel cannot be null.");
        Objects.requireNonNull(request.getRecipientAddress(), "Recipient address cannot be null or empty.");
        if(request.getRecipientAddress().trim().isEmpty()){
            throw new IllegalArgumentException("Recipient address cannot be empty.");
        }
        // Add more specific validations as needed:
        // - Validate recipientAddress format based on channel (e.g., email format, phone number format)
        // - Ensure templateId is provided if templates are mandatory
        // - Check payload structure if certain keys are expected for a given channel/template
        logger.debug("Notification request ID: {} passed validation.", request.getNotificationId());
    }

    /**
     * Creates an initial {@link NotificationLog} entity from a {@link NotificationRequest}.
     * The status is set to PENDING by default.
     *
     * @param request The notification request.
     * @return A new {@link NotificationLog} instance.
     */
    private NotificationLog createInitialLogEntry(NotificationRequest request) {
        NotificationLog logEntry = new NotificationLog();
        logEntry.setNotificationRequestId(request.getNotificationId());
        logEntry.setUserId(request.getUserId());
        logEntry.setChannel(request.getChannel());
        logEntry.setRecipientAddress(request.getRecipientAddress());
        logEntry.setTemplateId(request.getTemplateId());
        logEntry.setStatus(NotificationLog.NotificationStatus.PENDING); // Initial status
        logEntry.setAttemptCount(0); // Will be incremented by consumer on first attempt

        // Store the original payload and metadata as Maps
        // JPA/Hibernate with @JdbcTypeCode(SqlTypes.JSON) will handle Map to JSONB conversion.
        logEntry.setRequestPayload(request.getPayload());
        logEntry.setMetadata(request.getMetadata());

        // createdAt and updatedAt will be set by @PrePersist in NotificationLog entity
        return logEntry;
    }
}

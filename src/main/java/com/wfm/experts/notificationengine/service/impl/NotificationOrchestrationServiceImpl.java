package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.NotificationLog;
import com.wfm.experts.notificationengine.exception.NotificationProcessingException; // Ensure this exception exists
import com.wfm.experts.notificationengine.producer.NotificationProducer;
import com.wfm.experts.notificationengine.repository.NotificationLogRepository;
import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
// Import TenantContext if you were to explicitly set tenantId on NotificationRequest here,
// but current design has NotificationProducerImpl handle that for headers.
// import com.wfm.experts.tenancy.TenantContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

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
     * 1. Validates the notification request.
     * 2. Creates and saves an initial {@link NotificationLog} with PENDING status.
     * The tenant for this log is determined by the TenantContext active during this HTTP request.
     * 3. Publishes the request to RabbitMQ via {@link NotificationProducer}.
     * The NotificationProducer will read the TenantContext to add a tenantId header to the message.
     * 4. If publishing to RabbitMQ fails critically, updates the log status to FAILED.
     *
     * @param notificationRequest The DTO containing details of the notification to be sent.
     * This DTO itself does not need to carry the tenantId, as it's handled
     * by TenantContext on this (producer) side and message headers for consumers.
     * @throws IllegalArgumentException if the notificationRequest is invalid.
     * @throws NotificationProcessingException if a non-recoverable error occurs during processing.
     * @throws RuntimeException for other critical errors like messaging system failure.
     */
    @Override
    @Transactional // Ensures DB save and MQ publish attempt are within one transaction.
    // If MQ publish fails and throws an exception, the DB transaction for the log will roll back.
    // If MQ publish succeeds but a later part of *this method* fails, it also rolls back.
    // Note: This does not make DB and MQ operations a distributed XA transaction.
    public void processNotificationRequest(NotificationRequest notificationRequest) {
        validateNotificationRequest(notificationRequest);

        // TenantContext should be set by TenantFilter at this point.
        // The NotificationLog will be saved to the correct tenant's schema.
        logger.info("Processing notification request ID: {} for tenant: {}",
                notificationRequest.getNotificationId(), com.wfm.experts.tenancy.TenantContext.getTenant()); // Logging current tenant for verification

        NotificationLog logEntry = createInitialLogEntry(notificationRequest);

        try {
            // Save the initial log entry with PENDING status
            // This operation uses the TenantContext set by TenantFilter
            notificationLogRepository.save(logEntry);
            logger.debug("Notification log saved with PENDING status for ID: {}", logEntry.getNotificationRequestId());

            // Send the notification to the message queue.
            // NotificationProducerImpl will get TenantContext.getTenant() and add it as a header.
            notificationProducer.sendNotification(notificationRequest);
            logger.info("Notification request ID: {} successfully handed off to producer.", notificationRequest.getNotificationId());

            // The log status will be updated by consumers upon successful sending or definitive failure.
            // If sendNotification() itself throws an AmqpException (e.g., broker down),
            // the @Transactional annotation will cause a rollback of the log save.
            // The catch block below handles this scenario to update the log to FAILED if possible.

        } catch (Exception e) { // Catching general Exception to include AmqpException and others
            logger.error("Error during processing or publishing notification ID: {}. Error: {}",
                    notificationRequest.getNotificationId(), e.getMessage(), e);

            // Attempt to update the log entry to FAILED status if it was already persisted
            // This part might not execute if the initial save failed and transaction rolled back.
            // If the initial save succeeded but producer.sendNotification failed, this update is important.
            try {
                // Re-fetch or use the existing logEntry if appropriate, ensuring it's managed
                Optional<NotificationLog> existingLogOpt = notificationLogRepository.findByNotificationRequestId(logEntry.getNotificationRequestId());
                if(existingLogOpt.isPresent()){
                    NotificationLog logToUpdate = existingLogOpt.get();
                    logToUpdate.setStatus(NotificationLog.NotificationStatus.FAILED);
                    logToUpdate.setStatusMessage("Failed to publish to message queue or critical processing error: " + e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)));
                    logToUpdate.setFailedAt(LocalDateTime.now());
                    notificationLogRepository.save(logToUpdate);
                    logger.warn("Updated notification log to FAILED for ID: {}", logEntry.getNotificationRequestId());
                } else {
                    logger.warn("Could not find log entry for ID: {} to mark as FAILED after publishing error. Initial save might have rolled back.", logEntry.getNotificationRequestId());
                }
            } catch (Exception logUpdateException) {
                logger.error("Further error while trying to update log to FAILED for ID: {}. Error: {}",
                        logEntry.getNotificationRequestId(), logUpdateException.getMessage(), logUpdateException);
            }

            // Re-throw a specific or general runtime exception
            // to signal the failure to the caller (e.g., the controller).
            if (e instanceof IllegalArgumentException) { // Already handled by controller for 400
                throw e;
            }
            throw new NotificationProcessingException("Failed to process notification request for ID: " +
                    notificationRequest.getNotificationId() + ". Reason: " + e.getMessage(), e);
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
        if (!StringUtils.hasText(request.getNotificationId())) { // Use StringUtils for better null/empty/whitespace check
            throw new IllegalArgumentException("Notification ID cannot be null or empty.");
        }
        Objects.requireNonNull(request.getChannel(), "Notification channel cannot be null.");

        // For IN_APP, recipientAddress can be null if userId is the primary target.
        // For other channels like EMAIL, PUSH_FCM, PUSH_APNS, recipientAddress is usually required.
        if (request.getChannel() != NotificationRequest.ChannelType.IN_APP) {
            if (!StringUtils.hasText(request.getRecipientAddress())) {
                throw new IllegalArgumentException("Recipient address cannot be null or empty for channel: " + request.getChannel());
            }
        } else { // For IN_APP, userId is mandatory
            if (!StringUtils.hasText(request.getUserId())) {
                throw new IllegalArgumentException("User ID cannot be null or empty for IN_APP notifications.");
            }
        }

        // Basic check for templateId if channel is not IN_APP (where content might be fully in payload)
        // Or if IN_APP also mandates templates, adjust this logic.
        if (request.getChannel() != NotificationRequest.ChannelType.IN_APP && !StringUtils.hasText(request.getTemplateId())) {
            // This rule might be too strict if you allow sending emails/pushes with content fully in payload.
            // Adjust based on your design. For now, assuming templateId is generally expected for non-IN_APP.
            // logger.warn("TemplateId is missing for channel {}. This might be acceptable if content is fully in payload.", request.getChannel());
        }

        logger.debug("Notification request ID: {} passed basic validation.", request.getNotificationId());
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
        logEntry.setAttemptCount(0); // Consumer will increment on first actual processing attempt

        logEntry.setRequestPayload(request.getPayload());
        logEntry.setMetadata(request.getMetadata());

        // createdAt and updatedAt will be set by @PrePersist/@PreUpdate in NotificationLog entity
        return logEntry;
    }
}

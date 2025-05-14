package com.wfm.experts.notificationengine.service.impl.channel;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.NotificationLog;
import com.wfm.experts.notificationengine.integration.push.FcmClient; // Using the interface
import com.wfm.experts.notificationengine.repository.NotificationLogRepository;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.notificationengine.service.channel.PushChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link PushChannelService}.
 * Handles the dispatch of push notifications using an FcmClient.
 */
@Service
public class PushChannelServiceImpl implements PushChannelService {

    private static final Logger logger = LoggerFactory.getLogger(PushChannelServiceImpl.class);

    private final FcmClient fcmClient; // Injected FcmClient (could be mock or real)
    private final TemplatingService templatingService;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${notification.push.default-language:en-US}")
    private String defaultPushLanguage;

    // Keys to look for in the NotificationRequest payload for direct push content
    private static final String PAYLOAD_KEY_PUSH_TITLE = "pushTitle";
    private static final String PAYLOAD_KEY_PUSH_BODY = "pushBody";
    // Default values if no title/body can be determined
    private static final String DEFAULT_FALLBACK_PUSH_TITLE = "New Notification";
    private static final String DEFAULT_FALLBACK_PUSH_BODY = "You have a new update.";


    @Autowired
    public PushChannelServiceImpl(FcmClient fcmClient,
                                  TemplatingService templatingService,
                                  NotificationLogRepository notificationLogRepository) {
        this.fcmClient = fcmClient;
        this.templatingService = templatingService;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Sends a push notification.
     * It fetches/renders template content (if applicable), then sends via FcmClient.
     * Updates the NotificationLog with the outcome.
     *
     * @param notificationRequest The request DTO containing notification details.
     * @throws PushNotificationSendingException if sending fails.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // New transaction for log updates
    public void sendPushNotification(NotificationRequest notificationRequest) throws PushNotificationSendingException {
        String targetDeviceTokenOrUserId = notificationRequest.getRecipientAddress();
        String templateId = notificationRequest.getTemplateId();
        String notificationId = notificationRequest.getNotificationId();

        logger.info("Attempting to send push notification for ID: {}, Target: {}", notificationId, targetDeviceTokenOrUserId);

        Optional<NotificationLog> logOptional = notificationLogRepository.findByNotificationRequestId(notificationId);
        if (logOptional.isEmpty()) {
            logger.error("NotificationLog not found for request ID: {}. Cannot proceed with push sending.", notificationId);
            throw new PushNotificationSendingException("NotificationLog not found for request ID: " + notificationId);
        }
        NotificationLog logEntry = logOptional.get();
        updateLogStatus(logEntry, NotificationLog.NotificationStatus.PROCESSING, "Attempting to render and send push notification.", null);

        try {
            String language = Optional.ofNullable(notificationRequest.getMetadata())
                    .map(metadata -> metadata.getOrDefault("language", defaultPushLanguage))
                    .orElse(defaultPushLanguage);

            String title = DEFAULT_FALLBACK_PUSH_TITLE;
            String body = DEFAULT_FALLBACK_PUSH_BODY;
            Map<String, String> dataPayloadForFcm = new HashMap<>(); // For custom key-value data in FCM

            // 1. Attempt to use templating service if a templateId is provided
            if (templateId != null && !templateId.trim().isEmpty()) {
                Optional<TemplatingService.RenderedTemplateContent> renderedContentOpt = templatingService.getAndRenderTemplate(
                        templateId,
                        notificationRequest.getChannel(), // PUSH_FCM or PUSH_APNS
                        language,
                        notificationRequest.getPayload()
                );

                if (renderedContentOpt.isPresent()) {
                    TemplatingService.RenderedTemplateContent renderedContent = renderedContentOpt.get();
                    title = renderedContent.getSubject().orElse(title); // Use template subject as push title
                    body = renderedContent.getBody();                   // Use template body as push body
                    logger.debug("Rendered push content from template ID: {} for notification ID: {}", templateId, notificationId);
                } else {
                    logger.warn("Template ID: {} not found or failed to render for push notification ID: {}. Will use direct payload or defaults.", templateId, notificationId);
                }
            }

            // 2. Override with direct payload values if they exist, giving them precedence for push content
            // The payload in NotificationRequest is Map<String, Object>
            // The dataPayload for FCM is typically Map<String, String>
            if (notificationRequest.getPayload() != null) {
                title = String.valueOf(notificationRequest.getPayload().getOrDefault(PAYLOAD_KEY_PUSH_TITLE, title));
                body = String.valueOf(notificationRequest.getPayload().getOrDefault(PAYLOAD_KEY_PUSH_BODY, body));

                // Populate dataPayloadForFcm from the main payload, converting values to String
                // Exclude keys used for title and body to avoid redundancy if they are also in the main data section of FCM.
                notificationRequest.getPayload().forEach((key, value) -> {
                    if (!key.equals(PAYLOAD_KEY_PUSH_TITLE) && !key.equals(PAYLOAD_KEY_PUSH_BODY) && value != null) {
                        dataPayloadForFcm.put(key, String.valueOf(value));
                    }
                });
                logger.debug("Extracted direct payload for push notification ID: {}. Title: '{}', Body: '{}', Data: {}", notificationId, title, body, dataPayloadForFcm);
            }


            // For this example, we assume 'targetDeviceTokenOrUserId' is the actual device token.
            // In a real system: if it's a userId, you'd look up device tokens.
            // If it's a topic, you'd use an FCM client method for topic messaging.
            String deviceToken = targetDeviceTokenOrUserId;

            if (deviceToken == null || deviceToken.trim().isEmpty()) {
                updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Device token is null or empty.", null);
                throw new IllegalArgumentException("Device token is null or empty for push notification ID: " + notificationId);
            }

            // 3. Send via FcmClient
            String providerMessageId = fcmClient.sendPushNotification(deviceToken, title, body, dataPayloadForFcm);
            logger.info("Push notification 'sent' successfully via FcmClient for ID: {}. Provider Message ID: {}", notificationId, providerMessageId);

            updateLogStatus(logEntry, NotificationLog.NotificationStatus.SENT, "Push notification dispatched successfully.", providerMessageId);

        } catch (IllegalArgumentException e) { // Catch specific validation errors before FCM call
            logger.error("Invalid argument for push notification ID: {}: {}", notificationId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Invalid argument: " + e.getMessage(), null);
            throw new PushNotificationSendingException("Failed due to invalid argument: " + e.getMessage(), e);
        } catch (TemplatingService.TemplateProcessingException e) {
            logger.error("Template processing error for push notification ID: {}: {}", notificationId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Template processing error: " + e.getMessage(), null);
            throw new PushNotificationSendingException("Failed due to template processing error: " + e.getMessage(), e);
        } catch (FcmClient.FcmSendingException e) { // Catching the specific exception from our FcmClient
            logger.error("Failed to send push notification via FcmClient for ID: {}. Target: {}. Error: {}", notificationId, targetDeviceTokenOrUserId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "FCM sending failed: " + e.getMessage(), null);
            throw new PushNotificationSendingException("Failed to send push notification to " + targetDeviceTokenOrUserId + ": " + e.getMessage(), e);
        } catch (Exception e) { // Catch-all for other unexpected errors
            logger.error("Unexpected error during push notification sending for ID: {}. Target: {}. Error: {}", notificationId, targetDeviceTokenOrUserId, e.getMessage(), e);
            updateLogStatus(logEntry, NotificationLog.NotificationStatus.FAILED, "Unexpected error: " + e.getMessage(), null);
            throw new PushNotificationSendingException("Unexpected error sending push notification to " + targetDeviceTokenOrUserId + ": " + e.getMessage(), e);
        }
    }

    private void updateLogStatus(NotificationLog logEntry, NotificationLog.NotificationStatus status, String statusMessage, String providerMessageId) {
        try {
            logEntry.setStatus(status);
            if (statusMessage != null) {
                // Ensure statusMessage is not excessively long for the database column
                logEntry.setStatusMessage(statusMessage.substring(0, Math.min(statusMessage.length(), 2000)));
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
            notificationLogRepository.saveAndFlush(logEntry); // Persist changes immediately
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to update notification log for request ID: {}. Status: {}, Error: {}",
                    logEntry.getNotificationRequestId(), status, e.getMessage(), e);
            // This is a secondary failure (logging the primary failure). Log it with high importance.
        }
    }
}

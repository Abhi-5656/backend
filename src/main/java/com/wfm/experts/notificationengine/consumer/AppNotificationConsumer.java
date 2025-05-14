package com.wfm.experts.notificationengine.consumer;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl; // Added import
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header; // Added import
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message listener (consumer) for In-App notifications.
 * This consumer processes messages from the in-app notification queue and
 * explicitly handles tenant context setup from message headers.
 */
@Component
public class AppNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AppNotificationConsumer.class);

    private final AppNotificationService appNotificationService;

    @Value("${rabbitmq.queue.in_app}") // Ensure this property is in your application.yml
    private String inAppQueueName;

    @Autowired
    public AppNotificationConsumer(AppNotificationService appNotificationService) {
        this.appNotificationService = appNotificationService;
    }

    /**
     * Handles incoming in-app notification requests from the RabbitMQ queue.
     * Sets and clears TenantContext based on the tenant ID from message headers.
     *
     * @param notificationRequest The deserialized {@link NotificationRequest} payload of the message.
     * @param tenantIdFromHeader  The tenant ID extracted from the 'X-Tenant-ID' message header.
     */
    @RabbitListener(queues = "${rabbitmq.queue.in_app}")
    public void handleInAppNotification(
            @Payload NotificationRequest notificationRequest,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = false) String tenantIdFromHeader) {

        if (notificationRequest == null) {
            logger.warn("Received a null in-app notification request payload. Message will be acknowledged and ignored.");
            return;
        }

        if (tenantIdFromHeader == null || tenantIdFromHeader.trim().isEmpty()) {
            logger.error("CRITICAL: Tenant ID header ('{}') is missing or empty in message for NotificationRequest ID: {}. " +
                            "This in-app notification cannot be processed correctly. Message will be rejected.",
                    NotificationProducerImpl.TENANT_ID_HEADER, notificationRequest.getNotificationId());
            throw new AmqpRejectAndDontRequeueException("Tenant ID header ('" + NotificationProducerImpl.TENANT_ID_HEADER + "') is missing or empty in the message.");
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' from header for processing in-app notification ID: {}",
                tenantIdFromHeader, notificationRequest.getNotificationId());

        try {
            // Optional: Validate that the channel is indeed IN_APP.
            if (notificationRequest.getChannel() != NotificationRequest.ChannelType.IN_APP) {
                logger.warn("Received message on in-app queue with incorrect channel type: {} for notification ID: {}. Tenant: {}. Processing anyway as in-app, but this might indicate a routing issue.",
                        notificationRequest.getChannel(), notificationRequest.getNotificationId(), tenantIdFromHeader);
                // Depending on strictness, you might choose to reject or process.
                // For now, processing as IN_APP.
            }

            logger.info("Processing in-app notification request ID: '{}', for User ID: '{}', Tenant (from header): '{}'",
                    notificationRequest.getNotificationId(),
                    notificationRequest.getUserId(),
                    tenantIdFromHeader);

            // Delegate to the AppNotificationService.
            // AppNotificationServiceImpl and its underlying repositories will use TenantContext.getTenant()
            // which has been set from the header.
            appNotificationService.createAppNotification(notificationRequest);

            logger.info("Successfully processed and created in-app notification for ID: '{}', User ID: '{}', Tenant (from header): '{}'",
                    notificationRequest.getNotificationId(), notificationRequest.getUserId(), tenantIdFromHeader);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments encountered while processing in-app notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Invalid data for in-app notification: " + e.getMessage(), e);
        } catch (Exception e) { // Catch any other exceptions from AppNotificationService or other logic
            logger.error("Unexpected error processing in-app notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing in-app notification: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing in-app notification ID: {}", notificationRequest.getNotificationId());
        }
    }
}
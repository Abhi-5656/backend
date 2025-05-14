package com.wfm.experts.notificationengine.consumer;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.tenancy.TenantContext; // For logging current context if needed, but not for setting/clearing here
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message listener (consumer) for In-App notifications.
 * This consumer processes messages from the in-app notification queue.
 *
 * CRITICAL ASSUMPTION: This version assumes that an external mechanism
 * (e.g., a Spring AMQP interceptor or AOP advice) is responsible for:
 * 1. Extracting the tenant ID from the message headers.
 * 2. Calling TenantContext.setTenant() BEFORE this handleInAppNotification method is invoked.
 * 3. Calling TenantContext.clear() AFTER this handleInAppNotification method completes or errors.
 *
 * If such a mechanism is not in place, this consumer will not function correctly
 * in a multi-tenant environment.
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
     * Relies on an external mechanism to have set the TenantContext for the current thread.
     *
     * @param notificationRequest The deserialized {@link NotificationRequest} payload of the message.
     */
    @RabbitListener(queues = "${rabbitmq.queue.in_app}")
    public void handleInAppNotification(
            @Payload NotificationRequest notificationRequest) {

        if (notificationRequest == null) {
            logger.warn("Received a null in-app notification request payload. Message will be acknowledged and ignored.");
            return;
        }

        // Log the state of TenantContext as seen by this method, for debugging.
        // This does NOT set or clear the context; it assumes it's already set.
        String tenantIdFromContext = TenantContext.getTenant();
        if (tenantIdFromContext == null || tenantIdFromContext.trim().isEmpty()) {
            logger.error("CRITICAL PRE-CONDITION FAILED: TenantContext is null or empty at the start of handleInAppNotification for request ID: {}. " +
                            "The assumed external context-setting mechanism may not be working. Rejecting message.",
                    notificationRequest.getNotificationId());
            throw new AmqpRejectAndDontRequeueException("TenantContext not properly initialized for consumer thread.");
        }

        logger.debug("Processing in-app notification ID: {} within (externally set) TenantContext: {}",
                notificationRequest.getNotificationId(), tenantIdFromContext);

        try {
            // Optional: Validate that the channel is indeed IN_APP.
            if (notificationRequest.getChannel() != NotificationRequest.ChannelType.IN_APP) {
                logger.warn("Received message on in-app queue with incorrect channel type: {} for notification ID: {}. Tenant: {}. Ignoring, but this indicates a routing issue.",
                        notificationRequest.getChannel(), notificationRequest.getNotificationId(), tenantIdFromContext);
                return; // Acknowledge and ignore.
            }

            logger.info("Processing in-app notification request ID: '{}', for User ID: '{}', Tenant (from context): '{}'",
                    notificationRequest.getNotificationId(),
                    notificationRequest.getUserId(),
                    tenantIdFromContext); // Using tenantIdFromContext for logging

            // Delegate to the AppNotificationService.
            // AppNotificationServiceImpl and its underlying repositories will use TenantContext.getTenant()
            // which should return the value set by the external mechanism.
            appNotificationService.createAppNotification(notificationRequest);

            logger.info("Successfully processed and created in-app notification for ID: '{}', User ID: '{}', Tenant (from context): '{}'",
                    notificationRequest.getNotificationId(), notificationRequest.getUserId(), tenantIdFromContext);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments encountered while processing in-app notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromContext, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Invalid data for in-app notification: " + e.getMessage(), e);
        } catch (Exception e) { // Catch any other exceptions from AppNotificationService or other logic
            logger.error("Unexpected error processing in-app notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromContext, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing in-app notification: " + e.getMessage(), e);
        }
        // The external mechanism (if it exists) is responsible for TenantContext.clear() in its finally block.
    }
}

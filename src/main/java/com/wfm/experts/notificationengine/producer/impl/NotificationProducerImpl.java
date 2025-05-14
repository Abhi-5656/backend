package com.wfm.experts.notificationengine.producer.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.producer.NotificationProducer;
import com.wfm.experts.tenancy.TenantContext; // Ensure this import is correct for your TenantContext
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link NotificationProducer}.
 * This service publishes notification requests to a RabbitMQ exchange,
 * including the current tenant ID (obtained from TenantContext) as a message header.
 */
@Service
public class NotificationProducerImpl implements NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducerImpl.class);

    /**
     * Custom header name for passing the tenant ID with the AMQP message.
     * This constant can be used by consumers to retrieve the header.
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    // Routing key prefixes from application.yml
    @Value("${rabbitmq.routingkey.email_prefix:notification.email}")
    private String emailRoutingKeyPrefix;

    // SMS related properties are intentionally omitted as per previous instructions
    // @Value("${rabbitmq.routingkey.sms_prefix:notification.sms}")
    // private String smsRoutingKeyPrefix;

    @Value("${rabbitmq.routingkey.push_prefix:notification.push}")
    private String pushRoutingKeyPrefix;


    @Autowired
    public NotificationProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        // RabbitTemplate should be pre-configured with a JSON message converter
        // and publisher confirms/returns via RabbitMQConfig.
    }

    /**
     * Sends the notification request to RabbitMQ.
     * It retrieves the current tenant ID from {@link TenantContext} and adds it
     * as a header to the AMQP message. The {@link NotificationRequest} DTO itself
     * does not carry the tenantId.
     *
     * @param notificationRequest The notification data to send.
     */
    @Override
    public void sendNotification(NotificationRequest notificationRequest) {
        if (notificationRequest == null) {
            logger.warn("Attempted to send a null notification request. Ignoring.");
            return;
        }

        // Retrieve the tenantId from the context established by TenantFilter/JwtAuthenticationFilter
        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("CRITICAL: TenantId is not available in TenantContext for notification ID: {}. " +
                            "Message will be sent without tenant header, which WILL cause processing issues on the consumer side " +
                            "if the consumer relies on this header to set its own TenantContext.",
                    notificationRequest.getNotificationId());
            // Depending on your application's strictness, you might throw an exception here:
            // throw new IllegalStateException("TenantId not found in context. Cannot reliably send notification for ID: " + notificationRequest.getNotificationId());
        }

        String routingKey = determineRoutingKey(notificationRequest.getChannel());
        if (routingKey == null) {
            logger.error("Could not determine routing key for channel: {} for notification ID: {}. Notification will not be sent.",
                    notificationRequest.getChannel(), notificationRequest.getNotificationId());
            return;
        }

        // Use the notificationRequest.getNotificationId() for correlation data for publisher confirms
        CorrelationData correlationData = new CorrelationData(notificationRequest.getNotificationId());

        logger.info("Preparing to send notification ID: '{}' to exchange: '{}' with routing key: '{}'. Tenant ID to be added as header: '{}'",
                notificationRequest.getNotificationId(), notificationExchangeName, routingKey, currentTenantId);

        try {
            // Use a final variable for the tenantId to be accessible in the lambda
            final String tenantIdHeaderValue = currentTenantId;

            MessagePostProcessor messagePostProcessor = message -> {
                // Set the unique message ID (good for tracing within RabbitMQ)
                message.getMessageProperties().setMessageId(notificationRequest.getNotificationId());

                // Add the tenant ID as a custom header
                if (tenantIdHeaderValue != null && !tenantIdHeaderValue.trim().isEmpty()) {
                    message.getMessageProperties().setHeader(TENANT_ID_HEADER, tenantIdHeaderValue);
                } else {
                    // Log if tenantId is missing, as consumer might rely on it
                    logger.warn("TenantId is null or empty when creating message for notification ID: {}. Header '{}' will not be set.",
                            notificationRequest.getNotificationId(), TENANT_ID_HEADER);
                }
                // Example: Set application ID if needed
                // message.getMessageProperties().setAppId("wfm-backend-notification-service");
                return message;
            };

            // The NotificationRequest object will be converted to JSON by the configured MessageConverter
            rabbitTemplate.convertAndSend(
                    notificationExchangeName,
                    routingKey,
                    notificationRequest,    // The DTO payload
                    messagePostProcessor,   // To add headers
                    correlationData         // For publisher confirms
            );

            logger.debug("Notification ID: '{}' successfully passed to RabbitTemplate for publishing.", notificationRequest.getNotificationId());

        } catch (AmqpException e) {
            logger.error("Failed to send notification ID: '{}' to RabbitMQ. Exchange: '{}', RoutingKey: '{}'. Error: {}",
                    notificationRequest.getNotificationId(), notificationExchangeName, routingKey, e.getMessage(), e);
            // Consider specific exception handling or re-throwing a custom application exception
            throw e;
        }
    }

    /**
     * Determines the appropriate routing key based on the notification channel.
     *
     * @param channel The channel type from {@link NotificationRequest.ChannelType}.
     * @return The routing key string, or null if the channel is unsupported.
     */
    private String determineRoutingKey(NotificationRequest.ChannelType channel) {
        if (channel == null) {
            return null;
        }
        // Using a simple ".default" suffix for the event type part of the routing key.
        // In a more complex system, this could be more dynamic, e.g., based on notificationRequest.getTemplateId()
        // or another field indicating the specific event (e.g., "user.registered", "order.confirmed").
        String eventTypeSuffix = ".default";

        return switch (channel) {
            case EMAIL -> emailRoutingKeyPrefix + eventTypeSuffix; // e.g., "notification.email.default"
            // SMS case is omitted as per previous instructions
            case PUSH_FCM, PUSH_APNS -> // Assuming FCM and APNS might share a prefix or initial queue for simplicity
                    pushRoutingKeyPrefix + eventTypeSuffix;  // e.g., "notification.push.default"
            default -> {
                logger.warn("Unsupported notification channel type for routing key determination: {}", channel);
                yield null;
            }
        };
    }
}

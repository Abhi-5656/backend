package com.wfm.experts.notificationengine.producer.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.producer.NotificationProducer;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducerImpl implements NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducerImpl.class);

    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    @Value("${rabbitmq.routingkey.email_prefix:notification.email}")
    private String emailRoutingKeyPrefix;

    @Value("${rabbitmq.routingkey.push_prefix:notification.push}")
    private String pushRoutingKeyPrefix;

    @Value("${rabbitmq.routingkey.in_app_prefix:notification.inapp}") // New property for IN_APP
    private String inAppRoutingKeyPrefix;


    @Autowired
    public NotificationProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendNotification(NotificationRequest notificationRequest) {
        if (notificationRequest == null) {
            logger.warn("Attempted to send a null notification request. Ignoring.");
            return;
        }

        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("CRITICAL: TenantId is not available in TenantContext for notification ID: {}. " +
                            "Message will be sent without tenant header, which WILL cause processing issues on the consumer side.",
                    notificationRequest.getNotificationId());
        }

        String routingKey = determineRoutingKey(notificationRequest.getChannel());
        if (routingKey == null) {
            logger.error("Could not determine routing key for channel: {} for notification ID: {}. Notification will not be sent.",
                    notificationRequest.getChannel(), notificationRequest.getNotificationId());
            return;
        }

        CorrelationData correlationData = new CorrelationData(notificationRequest.getNotificationId());

        logger.info("Preparing to send notification ID: '{}' to exchange: '{}' with routing key: '{}'. Tenant ID to be added as header: '{}'",
                notificationRequest.getNotificationId(), notificationExchangeName, routingKey, currentTenantId);

        try {
            final String tenantIdHeaderValue = currentTenantId;
            MessagePostProcessor messagePostProcessor = message -> {
                message.getMessageProperties().setMessageId(notificationRequest.getNotificationId());
                if (tenantIdHeaderValue != null && !tenantIdHeaderValue.trim().isEmpty()) {
                    message.getMessageProperties().setHeader(TENANT_ID_HEADER, tenantIdHeaderValue);
                } else {
                    logger.warn("TenantId is null or empty when creating message for notification ID: {}. Header '{}' will not be set.",
                            notificationRequest.getNotificationId(), TENANT_ID_HEADER);
                }
                return message;
            };

            rabbitTemplate.convertAndSend(
                    notificationExchangeName,
                    routingKey,
                    notificationRequest,
                    messagePostProcessor,
                    correlationData
            );

            logger.debug("Notification ID: '{}' successfully passed to RabbitTemplate for publishing.", notificationRequest.getNotificationId());

        } catch (AmqpException e) {
            logger.error("Failed to send notification ID: '{}' to RabbitMQ. Exchange: '{}', RoutingKey: '{}'. Error: {}",
                    notificationRequest.getNotificationId(), notificationExchangeName, routingKey, e.getMessage(), e);
            throw e;
        }
    }

    private String determineRoutingKey(NotificationRequest.ChannelType channel) {
        if (channel == null) {
            return null;
        }
        String eventTypeSuffix = ".default"; // Or make this more dynamic

        return switch (channel) {
            case EMAIL -> emailRoutingKeyPrefix + eventTypeSuffix;
            case PUSH_FCM, PUSH_APNS -> pushRoutingKeyPrefix + eventTypeSuffix;
            case IN_APP -> // Added case for IN_APP
                    inAppRoutingKeyPrefix + eventTypeSuffix; // e.g., "notification.inapp.default"
            default -> {
                logger.warn("Unsupported notification channel type for routing key determination: {}", channel);
                yield null;
            }
        };
    }
}

package com.wfm.experts.notificationengine.consumer;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl;
import com.wfm.experts.notificationengine.service.channel.PushChannelService;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationConsumer.class);

    private final PushChannelService pushChannelService;

    @Value("${rabbitmq.queue.push}")
    private String pushQueueName;

    @Autowired
    public PushNotificationConsumer(PushChannelService pushChannelService) {
        this.pushChannelService = pushChannelService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.push}")
    public void handlePushNotification(
            @Payload NotificationRequest notificationRequest,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = false) String tenantIdFromHeader) {

        if (notificationRequest == null) {
            logger.warn("Received a null push notification request payload. Message will be acknowledged and ignored.");
            return;
        }

        if (tenantIdFromHeader == null || tenantIdFromHeader.trim().isEmpty()) {
            logger.error("CRITICAL: Tenant ID header ('{}') is missing or empty in message for NotificationRequest ID: {}. " +
                            "This push notification cannot be processed correctly. Message will be rejected.",
                    NotificationProducerImpl.TENANT_ID_HEADER, notificationRequest.getNotificationId());
            throw new AmqpRejectAndDontRequeueException("Tenant ID header ('" + NotificationProducerImpl.TENANT_ID_HEADER + "') is missing or empty in the message.");
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' from header for processing push notification ID: {}",
                tenantIdFromHeader, notificationRequest.getNotificationId());

        try {
            logger.info("Processing push notification request ID: '{}', Target: '{}', for Tenant: '{}'",
                    notificationRequest.getNotificationId(),
                    notificationRequest.getRecipientAddress(),
                    tenantIdFromHeader);

            if (notificationRequest.getRecipientAddress() == null || notificationRequest.getRecipientAddress().trim().isEmpty()) {
                logger.error("Recipient address (device token/user ID/topic) is missing for push notification ID: {}, Tenant: {}",
                        notificationRequest.getNotificationId(), tenantIdFromHeader);
                throw new IllegalArgumentException("Recipient address is required for push notification.");
            }

            pushChannelService.sendPushNotification(notificationRequest);

            logger.info("Successfully processed and dispatched push notification ID: '{}' for Tenant: '{}'",
                    notificationRequest.getNotificationId(), tenantIdFromHeader);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments encountered while processing push notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Invalid data for push notification: " + e.getMessage(), e);
        } catch (PushChannelService.PushNotificationSendingException e) {
            logger.error("PushNotificationSendingException occurred for notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Push notification sending failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error processing push notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing push notification: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing push notification ID: {}", notificationRequest.getNotificationId());
        }
    }
}

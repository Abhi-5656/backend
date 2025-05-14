package com.wfm.experts.notificationengine.consumer;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl;
import com.wfm.experts.notificationengine.service.channel.EmailChannelService;
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
public class EmailNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationConsumer.class);

    private final EmailChannelService emailChannelService;

    @Value("${rabbitmq.queue.email}")
    private String emailQueueName;

    @Autowired
    public EmailNotificationConsumer(EmailChannelService emailChannelService) {
        this.emailChannelService = emailChannelService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleEmailNotification(
            @Payload NotificationRequest notificationRequest,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = false) String tenantIdFromHeader) {

        if (notificationRequest == null) {
            logger.warn("Received a null notification request payload. Message will be acknowledged and ignored.");
            return;
        }

        if (tenantIdFromHeader == null || tenantIdFromHeader.trim().isEmpty()) {
            logger.error("CRITICAL: Tenant ID header ('{}') is missing or empty in message for NotificationRequest ID: {}. " +
                            "This notification cannot be processed correctly. Message will be rejected.",
                    NotificationProducerImpl.TENANT_ID_HEADER, notificationRequest.getNotificationId());
            throw new AmqpRejectAndDontRequeueException("Tenant ID header ('" + NotificationProducerImpl.TENANT_ID_HEADER + "') is missing or empty in the message.");
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' from header for processing notification ID: {}",
                tenantIdFromHeader, notificationRequest.getNotificationId());

        try {
            logger.info("Processing email notification request ID: '{}', Recipient: '{}', for Tenant: '{}'",
                    notificationRequest.getNotificationId(),
                    notificationRequest.getRecipientAddress(),
                    tenantIdFromHeader);

            emailChannelService.sendEmail(notificationRequest);

            logger.info("Successfully processed and dispatched email notification ID: '{}' for Tenant: '{}'",
                    notificationRequest.getNotificationId(), tenantIdFromHeader);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments encountered while processing email notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Invalid data for email notification: " + e.getMessage(), e);
        } catch (EmailChannelService.EmailSendingException e) {
            logger.error("EmailSendingException occurred for notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Email sending failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error processing email notification ID: '{}' for Tenant: '{}'. Error: {}. Rejecting message.",
                    notificationRequest.getNotificationId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing email notification: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing notification ID: {}", notificationRequest.getNotificationId());
        }
    }
}

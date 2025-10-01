package com.wfm.experts.modules.wfm.features.timesheet.producer.impl;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
import com.wfm.experts.modules.wfm.features.timesheet.producer.PunchProcessingProducer;
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

import java.util.UUID;

@Service
public class PunchProcessingProducerImpl implements PunchProcessingProducer {

    private static final Logger logger = LoggerFactory.getLogger(PunchProcessingProducerImpl.class);

    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    @Value("${rabbitmq.routingkey.punch_processing_prefix:punch.processing}")
    private String punchProcessingRoutingKeyPrefix;


    @Autowired
    public PunchProcessingProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendPunchProcessingRequest(PunchProcessingRequest request) {
        if (request == null) {
            logger.warn("Attempted to send a null punch processing request. Ignoring.");
            return;
        }

        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("CRITICAL: TenantId is not available in TenantContext for punch processing request. " +
                    "Message will be sent without tenant header, which WILL cause processing issues on the consumer side.");
        }

        String routingKey = punchProcessingRoutingKeyPrefix + ".work";
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        logger.info("Preparing to send punch processing request for employee: '{}', date: '{}' to exchange: '{}' with routing key: '{}'. Tenant ID to be added as header: '{}'",
                request.getEmployeeId(), request.getWorkDate(), notificationExchangeName, routingKey, currentTenantId);

        try {
            final String tenantIdHeaderValue = currentTenantId;
            MessagePostProcessor messagePostProcessor = message -> {
                if (tenantIdHeaderValue != null && !tenantIdHeaderValue.trim().isEmpty()) {
                    message.getMessageProperties().setHeader(TENANT_ID_HEADER, tenantIdHeaderValue);
                } else {
                    logger.warn("TenantId is null or empty when creating message for punch processing request. Header '{}' will not be set.",
                            TENANT_ID_HEADER);
                }
                return message;
            };

            rabbitTemplate.convertAndSend(
                    notificationExchangeName,
                    routingKey,
                    request,
                    messagePostProcessor,
                    correlationData
            );

            logger.debug("Punch processing request for employee: '{}', date: '{}' successfully passed to RabbitTemplate for publishing.", request.getEmployeeId(), request.getWorkDate());

        } catch (AmqpException e) {
            logger.error("Failed to send punch processing request for employee: '{}', date: '{}' to RabbitMQ. Exchange: '{}', RoutingKey: '{}'. Error: {}",
                    request.getEmployeeId(), request.getWorkDate(), notificationExchangeName, routingKey, e.getMessage(), e);
            throw e;
        }
    }
}
package com.wfm.experts.modules.wfm.employee.location.producer.impl;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;
import com.wfm.experts.modules.wfm.employee.location.producer.LocationTrackingProducer;
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
public class LocationTrackingProducerImpl implements LocationTrackingProducer {

    private static final Logger logger = LoggerFactory.getLogger(LocationTrackingProducerImpl.class);

    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    @Value("${rabbitmq.routingkey.employee_location_prefix:employee.location}")
    private String locationRoutingKeyPrefix;

    @Autowired
    public LocationTrackingProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendLocationUpdate(EmployeeLocationDTO locationDTO) {
        if (locationDTO == null) {
            logger.warn("Attempted to send a null location update. Ignoring.");
            return;
        }

        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("CRITICAL: TenantId is not available in TenantContext for location update. " +
                            "Message will be sent without tenant header, which WILL cause processing issues.",
                    locationDTO.getEmployeeId());
        }

        String routingKey = locationRoutingKeyPrefix + ".update";
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        logger.info("Publishing location update for employee: '{}' to exchange: '{}', routing key: '{}'. Tenant: '{}'",
                locationDTO.getEmployeeId(), notificationExchangeName, routingKey, currentTenantId);

        try {
            final String tenantIdHeaderValue = currentTenantId;
            MessagePostProcessor messagePostProcessor = message -> {
                if (tenantIdHeaderValue != null && !tenantIdHeaderValue.trim().isEmpty()) {
                    message.getMessageProperties().setHeader(TENANT_ID_HEADER, tenantIdHeaderValue);
                }
                message.getMessageProperties().setMessageId(correlationData.getId());
                return message;
            };

            rabbitTemplate.convertAndSend(
                    notificationExchangeName,
                    routingKey,
                    locationDTO,
                    messagePostProcessor,
                    correlationData
            );

        } catch (AmqpException e) {
            logger.error("Failed to send location update for employee: '{}' to RabbitMQ. Error: {}",
                    locationDTO.getEmployeeId(), e.getMessage(), e);
            throw e; // Re-throw to signal failure
        }
    }
}
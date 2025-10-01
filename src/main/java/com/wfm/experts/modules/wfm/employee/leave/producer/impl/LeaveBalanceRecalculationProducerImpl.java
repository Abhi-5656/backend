package com.wfm.experts.modules.wfm.employee.leave.producer.impl;

import com.wfm.experts.modules.wfm.employee.leave.producer.LeaveBalanceRecalculationProducer;
import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
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
public class LeaveBalanceRecalculationProducerImpl implements LeaveBalanceRecalculationProducer {

    private static final Logger logger = LoggerFactory.getLogger(LeaveBalanceRecalculationProducerImpl.class);

    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    @Value("${rabbitmq.routingkey.leave_balance_recalculation_prefix:leave.balance.recalculation}")
    private String leaveBalanceRecalculationRoutingKeyPrefix;


    @Autowired
    public LeaveBalanceRecalculationProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendLeaveBalanceRecalculationRequest(PunchProcessingRequest request) {
        if (request == null) {
            logger.warn("Attempted to send a null leave balance recalculation request. Ignoring.");
            return;
        }

        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("CRITICAL: TenantId is not available in TenantContext for leave balance recalculation request. " +
                    "Message will be sent without tenant header, which WILL cause processing issues on the consumer side.");
        }

        String routingKey = leaveBalanceRecalculationRoutingKeyPrefix + ".work";
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        logger.info("Preparing to send leave balance recalculation request for employee: '{}' to exchange: '{}' with routing key: '{}'. Tenant ID to be added as header: '{}'",
                request.getEmployeeId(), notificationExchangeName, routingKey, currentTenantId);

        try {
            final String tenantIdHeaderValue = currentTenantId;
            MessagePostProcessor messagePostProcessor = message -> {
                if (tenantIdHeaderValue != null && !tenantIdHeaderValue.trim().isEmpty()) {
                    message.getMessageProperties().setHeader(TENANT_ID_HEADER, tenantIdHeaderValue);
                } else {
                    logger.warn("TenantId is null or empty when creating message for leave balance recalculation request. Header '{}' will not be set.",
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

            logger.debug("Leave balance recalculation request for employee: '{}' successfully passed to RabbitTemplate for publishing.", request.getEmployeeId());

        } catch (AmqpException e) {
            logger.error("Failed to send leave balance recalculation request for employee: '{}' to RabbitMQ. Exchange: '{}', RoutingKey: '{}'. Error: {}",
                    request.getEmployeeId(), notificationExchangeName, routingKey, e.getMessage(), e);
            throw e;
        }
    }
}
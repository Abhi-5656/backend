package com.wfm.experts.modules.wfm.features.timesheet.consumer;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
import com.wfm.experts.notificationengine.producer.impl.NotificationProducerImpl;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PunchProcessingConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PunchProcessingConsumer.class);

    private final TimesheetCalculationService timesheetCalculationService;

    @Autowired
    public PunchProcessingConsumer(TimesheetCalculationService timesheetCalculationService) {
        this.timesheetCalculationService = timesheetCalculationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.punch_processing}")
    public void handlePunchProcessingRequest(
            @Payload PunchProcessingRequest request,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = true) String tenantIdFromHeader) {

        if (request == null) {
            logger.warn("Received a null punch processing request payload. Message will be acknowledged and ignored.");
            return;
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' from header for processing punch request for employee: {}, date: {}",
                tenantIdFromHeader, request.getEmployeeId(), request.getWorkDate());

        try {
            logger.info("Processing punch request for employee: '{}', date: '{}', Tenant: '{}'",
                    request.getEmployeeId(), request.getWorkDate(), tenantIdFromHeader);

            timesheetCalculationService.processPunchEvents(request.getEmployeeId(), request.getWorkDate());

            logger.info("Successfully processed punches for employee: '{}', date: '{}', Tenant: '{}'",
                    request.getEmployeeId(), request.getWorkDate(), tenantIdFromHeader);

        } catch (Exception e) {
            logger.error("Unexpected error processing punch request for employee: '{}', date: '{}', Tenant: '{}'. Error: {}. Rejecting message.",
                    request.getEmployeeId(), request.getWorkDate(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing punch request: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing punch request for employee: {}, date: {}", request.getEmployeeId(), request.getWorkDate());
        }
    }
}
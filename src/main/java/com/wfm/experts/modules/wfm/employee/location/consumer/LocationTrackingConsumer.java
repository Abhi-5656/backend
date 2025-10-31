package com.wfm.experts.modules.wfm.employee.location.consumer;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;
import com.wfm.experts.modules.wfm.employee.location.service.EmployeeLocationService;
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
public class LocationTrackingConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LocationTrackingConsumer.class);

    private final EmployeeLocationService locationService;

    @Autowired
    public LocationTrackingConsumer(EmployeeLocationService locationService) {
        this.locationService = locationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.employee_location}")
    public void handleLocationUpdate(
            @Payload EmployeeLocationDTO locationDTO,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = true) String tenantIdFromHeader) {

        if (locationDTO == null) {
            logger.warn("Received a null location update payload. Message will be acknowledged and ignored.");
            return;
        }

        if (tenantIdFromHeader == null || tenantIdFromHeader.trim().isEmpty()) {
            logger.error("CRITICAL: Tenant ID header is missing for location update for employee: {}. " +
                    "This message cannot be processed and will be rejected.", locationDTO.getEmployeeId());
            throw new AmqpRejectAndDontRequeueException("Tenant ID header is missing.");
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' for processing location update for employee: {}",
                tenantIdFromHeader, locationDTO.getEmployeeId());

        try {
            logger.info("Processing location update for employee: '{}', Tenant: '{}', Timestamp: {}",
                    locationDTO.getEmployeeId(), tenantIdFromHeader, locationDTO.getTimestamp());

            locationService.saveLocation(locationDTO);

            logger.info("Successfully processed location update for employee: '{}', Tenant: '{}'",
                    locationDTO.getEmployeeId(), tenantIdFromHeader);

        } catch (Exception e) {
            logger.error("Unexpected error processing location update for employee: '{}', Tenant: '{}'. Error: {}. Rejecting message.",
                    locationDTO.getEmployeeId(), tenantIdFromHeader, e.getMessage(), e);
            // Reject and don't re-queue to avoid poison messages
            throw new AmqpRejectAndDontRequeueException("Failed to process location update: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing location update for employee: {}", locationDTO.getEmployeeId());
        }
    }
}
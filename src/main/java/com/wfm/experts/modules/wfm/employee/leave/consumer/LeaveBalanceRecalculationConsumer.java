package com.wfm.experts.modules.wfm.employee.leave.consumer;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
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
public class LeaveBalanceRecalculationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LeaveBalanceRecalculationConsumer.class);

    private final LeaveAccrualService leaveAccrualService;

    @Autowired
    public LeaveBalanceRecalculationConsumer(LeaveAccrualService leaveAccrualService) {
        this.leaveAccrualService = leaveAccrualService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.leave_balance_recalculation}")
    public void handleLeaveBalanceRecalculationRequest(
            @Payload PunchProcessingRequest request,
            @Header(name = NotificationProducerImpl.TENANT_ID_HEADER, required = true) String tenantIdFromHeader) {

        if (request == null) {
            logger.warn("Received a null leave balance recalculation request payload. Message will be acknowledged and ignored.");
            return;
        }

        TenantContext.setTenant(tenantIdFromHeader);
        logger.debug("TenantContext set to '{}' from header for processing leave balance recalculation for employee: {}",
                tenantIdFromHeader, request.getEmployeeId());

        try {
            logger.info("Processing leave balance recalculation for employee: '{}', Tenant: '{}'",
                    request.getEmployeeId(), tenantIdFromHeader);

            leaveAccrualService.recalculateTotalLeaveBalance(request.getEmployeeId());

            logger.info("Successfully recalculated leave balance for employee: '{}', Tenant: '{}'",
                    request.getEmployeeId(), tenantIdFromHeader);

        } catch (Exception e) {
            logger.error("Unexpected error processing leave balance recalculation for employee: '{}', Tenant: '{}'. Error: {}. Rejecting message.",
                    request.getEmployeeId(), tenantIdFromHeader, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected error processing leave balance recalculation request: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
            logger.debug("TenantContext cleared after processing leave balance recalculation for employee: {}", request.getEmployeeId());
        }
    }
}
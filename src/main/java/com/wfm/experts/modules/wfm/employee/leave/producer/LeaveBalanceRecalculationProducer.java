package com.wfm.experts.modules.wfm.employee.leave.producer;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;

/**
 * Interface for services that produce (publish) leave balance recalculation messages
 * to a message broker like RabbitMQ.
 */
public interface LeaveBalanceRecalculationProducer {

    /**
     * Sends a leave balance recalculation request to the message broker.
     *
     * @param request The request containing the employeeId to process.
     */
    void sendLeaveBalanceRecalculationRequest(PunchProcessingRequest request);

}
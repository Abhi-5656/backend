package com.wfm.experts.modules.wfm.features.timesheet.producer;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;

/**
 * Interface for services that produce (publish) punch processing messages
 * to a message broker like RabbitMQ.
 */
public interface PunchProcessingProducer {

    /**
     * Sends a punch processing request to the message broker.
     *
     * @param request The request containing the employeeId and workDate to process.
     */
    void sendPunchProcessingRequest(PunchProcessingRequest request);

}
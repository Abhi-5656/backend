package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest;

/**
 * Service interface for orchestrating the notification process.
 * This service acts as an entry point for notification requests,
 * handles initial validation, logging, and then delegates to the
 * appropriate producer to send the message to the queue.
 */
public interface NotificationOrchestrationService {

    /**
     * Processes an incoming notification request.
     * This method is responsible for:
     * 1. Validating the notification request.
     * 2. (Optionally) Persisting an initial log of the notification attempt (e.g., with PENDING status).
     * 3. Handing off the notification request to a {@link com.wfm.experts.notificationengine.producer.NotificationProducer}
     * to be sent to the message queue.
     *
     * @param notificationRequest The DTO containing details of the notification to be sent.
     * @throws IllegalArgumentException if the notificationRequest is invalid (e.g., missing required fields).
     * @throws Exception if any other critical error occurs during the initial processing before queueing.
     */
    void processNotificationRequest(NotificationRequest notificationRequest) throws Exception;

    // You might add other methods here if needed, for example:
    // - Methods to handle batched notification requests.
    // - Methods to query the status of a previously submitted notification (though this might also be part of NotificationLogService).
}

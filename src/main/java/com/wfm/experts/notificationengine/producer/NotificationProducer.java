package com.wfm.experts.notificationengine.producer;

import com.wfm.experts.notificationengine.dto.NotificationRequest;

/**
 * Interface for services that produce (publish) notification messages
 * to a message broker like RabbitMQ.
 */
public interface NotificationProducer {

    /**
     * Sends a notification request to the message broker.
     *
     * @param notificationRequest The notification request DTO containing all
     * details for the notification.
     */
    void sendNotification(NotificationRequest notificationRequest);

}

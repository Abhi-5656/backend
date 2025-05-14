package com.wfm.experts.notificationengine.service.channel;

import com.wfm.experts.notificationengine.dto.NotificationRequest;

/**
 * Service interface for handling the dispatch of push notifications.
 * Implementations of this interface will interact with actual push notification providers
 * like Firebase Cloud Messaging (FCM), Apple Push Notification service (APNs), etc.
 */
public interface PushChannelService {

    /**
     * Sends a push notification based on the details provided in the NotificationRequest.
     * This method will typically:
     * 1. Use a {@link com.wfm.experts.notificationengine.service.TemplatingService}
     * to get the rendered title, body, and any other textual content for the push notification
     * using the templateId and payload from the notificationRequest.
     * 2. Determine the target device token(s) or topic. The {@code notificationRequest.getRecipientAddress()}
     * might hold a device token, a user ID (to look up tokens), or a topic name.
     * 3. Construct the platform-specific push notification payload (e.g., for FCM or APNs).
     * 4. Interact with a push notification client/library (e.g., Firebase Admin SDK)
     * to dispatch the push notification.
     * 5. Handle any exceptions that occur during the push notification sending process.
     * 6. (Optionally) Update a {@link com.wfm.experts.notificationengine.entity.NotificationLog}
     * with the status from the push provider (e.g., message ID, error codes).
     *
     * @param notificationRequest The DTO containing all necessary details for the push notification,
     * including target information (like device token or user ID),
     * templateId, and payload for template rendering and push content.
     * @throws PushNotificationSendingException if an error occurs specifically during the push notification dispatch
     * (e.g., invalid device token, API error from push provider).
     */
    void sendPushNotification(NotificationRequest notificationRequest) throws PushNotificationSendingException;

    /**
     * Custom exception for errors that occur specifically during push notification dispatch.
     */
    class PushNotificationSendingException extends RuntimeException {
        public PushNotificationSendingException(String message) {
            super(message);
        }

        public PushNotificationSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

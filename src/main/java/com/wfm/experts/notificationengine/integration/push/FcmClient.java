package com.wfm.experts.notificationengine.integration.push;

import java.util.Map;

/**
 * Interface for a client that interacts with Firebase Cloud Messaging (FCM)
 * or a similar push notification service.
 *
 * This abstraction allows for different implementations (e.g., a real FCM client
 * using Firebase Admin SDK, or a mock client for testing).
 */
public interface FcmClient {

    /**
     * Sends a push notification to a specific device token.
     *
     * @param deviceToken The registration token of the target device.
     * @param title       The title of the notification. Can be null if the notification
     * is data-only or if the title is part of the dataPayload.
     * @param body        The body/message of the notification. Can be null if the notification
     * is data-only or if the body is part of the dataPayload.
     * @param dataPayload A map of custom key-value pairs to send as the data payload
     * of the push notification. This data is typically received by
     * the client application when it's in the background or foreground.
     * Keys and values must be strings.
     * @return A message ID or a unique identifier from the push notification provider
     * if the message was successfully accepted for delivery.
     * @throws FcmSendingException if an error occurs while trying to send the notification
     * (e.g., authentication failure, invalid token, network issue).
     */
    String sendPushNotification(String deviceToken, String title, String body, Map<String, String> dataPayload) throws FcmSendingException;

    // Future methods could include:
    // - Sending to a topic: sendToTopic(String topic, String title, String body, Map<String, String> dataPayload)
    // - Sending to multiple tokens (batch send): sendToMultipleTokens(List<String> deviceTokens, String title, String body, Map<String, String> dataPayload)
    // - Sending messages with more complex FCM options (e.g., TTL, priority, sound, icon, click_action for Android, APNS specific fields)

    /**
     * Custom exception for errors that occur specifically during FCM dispatch.
     */
    class FcmSendingException extends RuntimeException {
        public FcmSendingException(String message) {
            super(message);
        }

        public FcmSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

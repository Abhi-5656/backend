package com.wfm.experts.notificationengine.integration.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component; // Or @Service if it has more complex logic/dependencies

import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of {@link FcmClient} for development and testing purposes.
 * This implementation does NOT actually send push notifications to FCM.
 * It simulates sending and can be configured to simulate errors for testing error paths.
 */
@Component // <<--- CRITICAL: This annotation makes it a Spring bean
public class MockFcmClientImpl implements FcmClient {

    private static final Logger logger = LoggerFactory.getLogger(MockFcmClientImpl.class);

    // Test tokens for simulating different scenarios
    public static final String VALID_TOKEN = "mock-valid-fcm-token";
    public static final String INVALID_TOKEN_FORMAT = "mock-invalid-format-fcm-token"; // Simulates a badly formatted token
    public static final String UNREGISTERED_TOKEN = "mock-unregistered-fcm-token"; // Simulates a token that FCM reports as not registered
    public static final String FCM_SERVER_ERROR_TOKEN = "mock-fcm-server-error-token"; // Simulates a temporary FCM server error

    @Override
    public String sendPushNotification(String deviceToken, String title, String body, Map<String, String> dataPayload) throws FcmSendingException {
        logger.info("MOCK FCM SEND :: Attempting to send push notification...");
        logger.info("MOCK FCM SEND :: To Device Token: {}", deviceToken);
        logger.info("MOCK FCM SEND :: Title: {}", title);
        logger.info("MOCK FCM SEND :: Body: {}", body);
        if (dataPayload != null && !dataPayload.isEmpty()) {
            logger.info("MOCK FCM SEND :: Data Payload: {}", dataPayload);
        } else {
            logger.info("MOCK FCM SEND :: Data Payload: (empty)");
        }

        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            logger.error("MOCK FCM SEND :: Device token is null or empty.");
            throw new FcmSendingException("Mock FCM: Device token cannot be null or empty.");
        }

        // Simulate different FCM responses based on the token
        switch (deviceToken) {
            case INVALID_TOKEN_FORMAT:
                logger.warn("MOCK FCM SEND :: Simulating 'InvalidRegistration' error for token: {}", deviceToken);
                throw new FcmSendingException("Mock FCM: Invalid device token format for token: " + deviceToken);

            case UNREGISTERED_TOKEN:
                logger.warn("MOCK FCM SEND :: Simulating 'Unregistered' error for token: {}", deviceToken);
                throw new FcmSendingException("Mock FCM: Device token is not registered or app uninstalled: " + deviceToken);

            case FCM_SERVER_ERROR_TOKEN:
                logger.warn("MOCK FCM SEND :: Simulating FCM internal server error for token: {}", deviceToken);
                throw new FcmSendingException("Mock FCM: Simulated FCM server error (e.g., UNAVAILABLE).");

            case VALID_TOKEN:
                // Simulate successful sending
                String mockMessageId = "mock-fcm-message-" + UUID.randomUUID().toString();
                logger.info("MOCK FCM SEND :: Successfully 'sent' push notification. Mock Message ID: {}", mockMessageId);
                return mockMessageId;

            default:
                // For any other token, assume it's valid for the mock
                String defaultMockMessageId = "mock-fcm-message-" + UUID.randomUUID().toString();
                logger.info("MOCK FCM SEND :: Successfully 'sent' push notification (default mock behavior). Mock Message ID: {}", defaultMockMessageId);
                return defaultMockMessageId;
        }
    }
}

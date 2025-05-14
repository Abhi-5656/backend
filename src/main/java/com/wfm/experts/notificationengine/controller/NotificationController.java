package com.wfm.experts.notificationengine.controller;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.exception.NotificationProcessingException;
import com.wfm.experts.notificationengine.service.NotificationOrchestrationService;
import jakarta.validation.Valid; // For JSR 380 bean validation
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling incoming notification requests.
 * This controller provides an endpoint to submit notifications to the engine for any channel.
 */
@RestController
@RequestMapping("/api/notifications") // Base path for notification-related endpoints
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationOrchestrationService notificationOrchestrationService;

    @Autowired
    public NotificationController(NotificationOrchestrationService notificationOrchestrationService) {
        this.notificationOrchestrationService = notificationOrchestrationService;
    }

    /**
     * Endpoint to submit a new notification request for any channel (EMAIL, PUSH_FCM, PUSH_APNS, IN_APP).
     * The request is validated and then passed to the orchestration service
     * for further processing and queuing. The 'channel' field in the NotificationRequest
     * DTO determines how it will be processed.
     *
     * @param notificationRequest The {@link NotificationRequest} DTO containing the details
     * of the notification to be sent.
     * @return A {@link ResponseEntity} indicating the outcome of the request submission.
     * - 202 Accepted: If the request is successfully received and queued for processing.
     * - 400 Bad Request: If the request is invalid (e.g., missing required fields).
     * - 500 Internal Server Error: If an unexpected error occurs during processing.
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@Valid @RequestBody NotificationRequest notificationRequest) {
        // @Valid annotation triggers JSR 380 bean validation if constraints are defined on NotificationRequest DTO.
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Received request to send notification with ID: {} for channel: {}",
                    notificationRequest.getNotificationId(), notificationRequest.getChannel());

            // Delegate to the orchestration service
            notificationOrchestrationService.processNotificationRequest(notificationRequest);

            response.put("message", "Notification request received and is being processed.");
            response.put("notificationId", notificationRequest.getNotificationId());
            logger.info("Notification request ID: {} successfully accepted for processing.", notificationRequest.getNotificationId());
            return ResponseEntity.accepted().body(response); // HTTP 202 Accepted

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid notification request for ID [{}]: {}",
                    notificationRequest.getNotificationId(), e.getMessage());
            response.put("error", "Invalid request parameters.");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response); // HTTP 400 Bad Request

        } catch (NotificationProcessingException e) {
            logger.error("Notification processing error for ID [{}]: {}",
                    notificationRequest.getNotificationId(), e.getMessage(), e);
            response.put("error", "Notification processing failed.");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500

        } catch (Exception e) {
            // Catch-all for any other unexpected errors
            logger.error("Unexpected error while processing notification request ID [{}]: {}",
                    notificationRequest != null ? notificationRequest.getNotificationId() : "N/A", e.getMessage(), e);
            response.put("error", "An unexpected error occurred.");
            response.put("message", "Failed to process notification request. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500
        }
    }

    // You could add other endpoints here later, for example:
    // - GET /api/v1/notifications/{notificationId}/status  (to query the status of a notification from NotificationLog)
}

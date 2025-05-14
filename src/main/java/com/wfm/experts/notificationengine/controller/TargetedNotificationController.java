package com.wfm.experts.notificationengine.controller;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // Keep this if still used directly elsewhere or by the DTO
import com.wfm.experts.notificationengine.dto.TargetedNotificationRequestDto; // Import the new DTO
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.tenancy.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Remove the inline DTO class from here if you moved it to its own file.

@RestController
@RequestMapping("/api/notifications/targeted")
public class TargetedNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(TargetedNotificationController.class);

    private final AppNotificationService appNotificationService;

    @Autowired
    public TargetedNotificationController(AppNotificationService appNotificationService) {
        this.appNotificationService = appNotificationService;
    }

    @PostMapping("/in-app/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendInAppNotificationToSpecificUsers(
            @Valid @RequestBody TargetedNotificationRequestDto targetedRequest) { // Use the imported DTO

        Map<String, Object> response = new HashMap<>();
        String currentTenantId = TenantContext.getTenant();

        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Targeted send failed: Tenant ID is missing from security context.");
            response.put("error", "Tenant context not found.");
            return ResponseEntity.status(500).body(response);
        }

        NotificationRequest contentRequest = targetedRequest.getNotificationContent();
        List<String> userIds = targetedRequest.getTargetUserIds();

        if (userIds == null || userIds.isEmpty()) {
            logger.warn("No target user IDs provided for targeted notification in tenant [{}].", currentTenantId);
            response.put("error", "No target user IDs provided.");
            return ResponseEntity.badRequest().body(response);
        }
        if (contentRequest == null) {
            logger.warn("Notification content is missing for targeted notification in tenant [{}].", currentTenantId);
            response.put("error", "Notification content is missing.");
            return ResponseEntity.badRequest().body(response);
        }
        contentRequest.setChannel(NotificationRequest.ChannelType.IN_APP);

        try {
            logger.info("Received request to send in-app notification to {} specific users in tenant: {}. Base Request ID: {}",
                    userIds.size(), currentTenantId, contentRequest.getNotificationId());

            List<AppNotification> sentNotifications = appNotificationService.createAppNotificationsForSpecificUsers(contentRequest, userIds);

            response.put("message", "In-app notifications initiated for " + sentNotifications.size() + " targeted users in tenant: " + currentTenantId);
            response.put("successfullyTargetedCount", sentNotifications.size());
            logger.info("Successfully initiated {} in-app notifications for specific users in tenant {}", sentNotifications.size(), currentTenantId);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.warn("Illegal state for targeted send in tenant [{}]: {}", currentTenantId, e.getMessage());
            response.put("error", "Targeted send precondition failed.");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error while sending targeted in-app notifications in tenant [{}]: {}",
                    currentTenantId, e.getMessage(), e);
            response.put("error", "An unexpected error occurred.");
            response.put("message", "Failed to send targeted notifications.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
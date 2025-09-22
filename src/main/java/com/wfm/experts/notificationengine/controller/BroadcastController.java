package com.wfm.experts.notificationengine.controller;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.tenancy.TenantContext; // To log the current tenant
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For method-level security
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/broadcast") // Base path for broadcast notifications
public class BroadcastController {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastController.class);

    private final AppNotificationService appNotificationService;

    @Autowired
    public BroadcastController(AppNotificationService appNotificationService) {
        this.appNotificationService = appNotificationService;
    }

    /**
     * Endpoint to submit a new in-app notification to be broadcast to all users
     * within the current authenticated user's tenant.
     *
     * The TenantContext is expected to be set by JwtAuthenticationFilter.
     *
     * @param broadcastRequest The {@link NotificationRequest} DTO. The `userId` field will be ignored for broadcast.
     * The `channel` should ideally be IN_APP or it will be treated as such.
     * @return A ResponseEntity indicating the outcome.
     */
    @PostMapping("/in-app")
//    @PreAuthorize("hasAuthority('ADMIN')") // Example: Only users with ADMIN role can broadcast
    public ResponseEntity<Map<String, Object>> broadcastInAppNotification(@Valid @RequestBody NotificationRequest broadcastRequest) {
        Map<String, Object> response = new HashMap<>();
        String currentTenantId = TenantContext.getTenant();

        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Broadcast failed: Tenant ID is missing from security context.");
            response.put("error", "Tenant context not found. Cannot determine broadcast scope.");
            return ResponseEntity.status(500).body(response);
        }

        try {
            // Ensure the channel is IN_APP or force it if this endpoint is specific
            if (broadcastRequest.getChannel() != NotificationRequest.ChannelType.IN_APP) {
                logger.warn("Broadcast request channel was {}. Forcing to IN_APP for this endpoint.", broadcastRequest.getChannel());
                broadcastRequest.setChannel(NotificationRequest.ChannelType.IN_APP);
            }
            // The userId in broadcastRequest can be ignored as this is a broadcast to all in the tenant.
            // The service method will handle how this is represented.
            broadcastRequest.setUserId(null); // Explicitly nullify or use a placeholder if your DTO/logic expects something

            logger.info("Received request to broadcast in-app notification for tenant: {} (Request ID: {})",
                    currentTenantId, broadcastRequest.getNotificationId());

            AppNotification representativeNotification = appNotificationService.createAndBroadcastAppNotification(broadcastRequest);

            response.put("message", "In-app notification broadcast initiated for tenant: " + currentTenantId);
            response.put("notificationContentId", representativeNotification.getId());
            response.put("broadcastTopic", "/topic/in-app-notifications/" + currentTenantId);
            logger.info("In-app notification (content ID: {}) broadcast to topic /topic/in-app-notifications/{} successfully initiated.",
                    representativeNotification.getId(), currentTenantId);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.warn("Illegal state for broadcast for tenant [{}]: {}", currentTenantId, e.getMessage());
            response.put("error", "Broadcast precondition failed.");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error while broadcasting in-app notification for tenant [{}]: {}",
                    currentTenantId, e.getMessage(), e);
            response.put("error", "An unexpected error occurred during broadcast.");
            response.put("message", "Failed to broadcast notification.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
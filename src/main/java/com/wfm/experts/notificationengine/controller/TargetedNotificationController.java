package com.wfm.experts.notificationengine.controller;

import com.wfm.experts.notificationengine.dto.TargetedNotificationRequestDto;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.notificationengine.dto.NotificationRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            @Valid @RequestBody TargetedNotificationRequestDto targetedRequest) {

        Map<String, Object> response = new HashMap<>();
        String currentTenantId = TenantContext.getTenant();

        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Targeted send failed: Tenant ID is missing from security context.");
            response.put("error", "Tenant context not found.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            logger.info("Admin in tenant [{}] sending targeted in-app notification to {} users. Base Request ID: {}",
                    currentTenantId, userIds.size(), contentRequest.getNotificationId());

            List<AppNotification> sentNotifications = appNotificationService.createAppNotificationsForSpecificUsers(contentRequest, userIds);

            response.put("message", "In-app notifications initiated for " + sentNotifications.size() + " targeted users in tenant: " + currentTenantId);
            response.put("successfullyTargetedCount", sentNotifications.size());
            logger.info("Admin in tenant [{}] successfully initiated {} in-app notifications for specific users.", currentTenantId, sentNotifications.size());
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.warn("Illegal state for targeted send in tenant [{}]: {}", currentTenantId, e.getMessage(), e);
            response.put("error", "Targeted send precondition failed.");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error while sending targeted in-app notifications in tenant [{}]: {}",
                    currentTenantId, e.getMessage(), e);
            response.put("error", "An unexpected error occurred.");
            response.put("message", "Failed to send targeted notifications.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<AppNotification>> getUnreadNotificationsForUser(
            @PathVariable String userId,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        logger.info("Admin fetching unread notifications for user: {} in tenant: {}", userId, TenantContext.getTenant());
        Page<AppNotification> notifications = appNotificationService.getUnreadNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<AppNotification>> getAllNotificationsForUser(
            @PathVariable String userId,
            @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable) {
        logger.info("Admin fetching all notifications for user: {} in tenant: {}", userId, TenantContext.getTenant());
        Page<AppNotification> notifications = appNotificationService.getAllNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCountForUser(@PathVariable String userId) { // Changed to Map<String, Object>
        logger.info("Admin fetching unread notification count for user: {} in tenant: {}", userId, TenantContext.getTenant());
        long count = appNotificationService.getUnreadNotificationCountForUser(userId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", userId); // userId is a String
        responseBody.put("unreadCount", count); // count is a long (which is fine for Object)

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/user/{userId}/notification/{notificationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppNotification> getNotificationByIdForUser(
            @PathVariable String userId,
            @PathVariable Long notificationId) {
        logger.info("Admin fetching notification by ID: {} for user: {} in tenant: {}", notificationId, userId, TenantContext.getTenant());
        Optional<AppNotification> notification = appNotificationService.getNotificationByIdAndUser(notificationId, userId);
        return notification
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/user/{userId}/notification/{notificationId}/mark-as-read")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AppNotification> markNotificationAsReadForUser(
            @PathVariable String userId,
            @PathVariable Long notificationId) {
        logger.info("Admin attempting to mark notification {} as read for user {} in tenant: {}", notificationId, userId, TenantContext.getTenant());
        Optional<AppNotification> updatedNotification = appNotificationService.markNotificationAsRead(notificationId, userId);
        return updatedNotification
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/user/{userId}/mark-multiple-as-read")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> markNotificationsAsReadForUser(
            @PathVariable String userId,
            @RequestBody List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("updatedCount", 0, "error", "notificationIds list cannot be empty"));
        }
        logger.info("Admin attempting to mark {} notifications as read for user {} in tenant: {}", notificationIds.size(), userId, TenantContext.getTenant());
        int count = appNotificationService.markNotificationsAsRead(notificationIds, userId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", userId);
        responseBody.put("updatedCount", count);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/user/{userId}/mark-all-as-read")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsReadForUser(@PathVariable String userId) {
        logger.info("Admin attempting to mark all notifications as read for user {} in tenant: {}", userId, TenantContext.getTenant());
        int count = appNotificationService.markAllNotificationsAsReadForUser(userId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", userId);
        responseBody.put("updatedCount", count);
        return ResponseEntity.ok(responseBody);
    }
}
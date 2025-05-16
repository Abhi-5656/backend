package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture; // Import CompletableFuture

public interface AppNotificationService {

    AppNotification createAppNotification(NotificationRequest notificationRequest);

    AppNotification createAndBroadcastAppNotification(NotificationRequest notificationRequest);

    List<AppNotification> createAppNotificationsForSpecificUsers(NotificationRequest baseNotificationRequest, List<String> targetUserIds);

    // Add this method signature to the interface
    CompletableFuture<AppNotification> processAndSendToSingleUserAsync(
            AppNotification commonContent, String targetUserId, String tenantId, String baseNotificationRequestId);

    // ... other existing methods ...
    Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable);
    Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable);
    long getUnreadNotificationCountForUser(String userId);
    Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId);
    int markNotificationsAsRead(List<Long> notificationIds, String userId);
    int markAllNotificationsAsReadForUser(String userId);
    Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId);
}
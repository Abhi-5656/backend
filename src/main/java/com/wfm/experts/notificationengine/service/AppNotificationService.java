package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AppNotificationService {

    AppNotification createAppNotification(NotificationRequest notificationRequest); // Existing for single user

    // New method for broadcasting to all users of the current tenant
    AppNotification createAndBroadcastAppNotification(NotificationRequest notificationRequest);

    // ... other existing methods
    Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable);
    Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable);
    long getUnreadNotificationCountForUser(String userId);
    Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId);
    int markNotificationsAsRead(List<Long> notificationIds, String userId);
    int markAllNotificationsAsReadForUser(String userId);
    Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId);
}
package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // To create AppNotification from
import com.wfm.experts.notificationengine.entity.AppNotification; // The entity this service manages
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing AppNotifications.
 * This includes creating new in-app notifications, fetching them for users,
 * and managing their read status.
 * This service also effectively acts as the "channel service" for IN_APP notifications.
 */
public interface AppNotificationService {

    /**
     * Creates and persists an in-app notification based on a general NotificationRequest.
     * This method would be called by the InAppNotificationConsumer.
     *
     * @param notificationRequest The original notification request DTO.
     * @return The created AppNotification entity.
     * @throws IllegalArgumentException if essential information (like userId, title, body) is missing.
     */
    AppNotification createAppNotification(NotificationRequest notificationRequest);

    /**
     * Retrieves a paginated list of unread in-app notifications for a specific user.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of unread AppNotification entities.
     */
    Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable);

    /**
     * Retrieves a paginated list of all (read and unread) in-app notifications for a specific user.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of AppNotification entities.
     */
    Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable);

    /**
     * Gets the count of unread in-app notifications for a specific user.
     *
     * @param userId The ID of the user.
     * @return The number of unread notifications.
     */
    long getUnreadNotificationCountForUser(String userId);

    /**
     * Marks a specific in-app notification as read for a user.
     *
     * @param notificationId The ID of the AppNotification to mark as read.
     * @param userId         The ID of the user (to ensure they own the notification).
     * @return An Optional containing the updated AppNotification if found and marked as read,
     * otherwise empty (e.g., if notification not found or already read).
     * @throws com.wfm.experts.notificationengine.exception.AppNotificationNotFoundException if the notification doesn't exist.
     * @throws com.wfm.experts.notificationengine.exception.UnauthorizedNotificationAccessException if the user tries to access a notification not belonging to them.
     */
    Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId);

    /**
     * Marks multiple in-app notifications as read for a user.
     *
     * @param notificationIds A list of AppNotification IDs to mark as read.
     * @param userId          The ID of the user.
     * @return The number of notifications successfully marked as read.
     */
    int markNotificationsAsRead(List<Long> notificationIds, String userId);

    /**
     * Marks all unread in-app notifications for a specific user as read.
     *
     * @param userId The ID of the user.
     * @return The number of notifications successfully marked as read.
     */
    int markAllNotificationsAsReadForUser(String userId);

    /**
     * Retrieves a specific in-app notification by its ID, ensuring it belongs to the user.
     *
     * @param notificationId The ID of the AppNotification.
     * @param userId         The ID of the user.
     * @return An Optional containing the AppNotification.
     */
    Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId);


    /**
     * (Optional) Deletes an in-app notification.
     * Consider if users should be able to delete individual notifications or if it's an admin/system task.
     *
     * @param notificationId The ID of the AppNotification to delete.
     * @param userId         The ID of the user (to ensure they own the notification for deletion).
     * @return true if deleted successfully, false otherwise.
     * @throws com.wfm.experts.notificationengine.exception.AppNotificationNotFoundException if the notification doesn't exist.
     * @throws com.wfm.experts.notificationengine.exception.UnauthorizedNotificationAccessException if the user tries to delete a notification not belonging to them.
     */
    // boolean deleteNotification(Long notificationId, String userId);

    /**
     * (Optional) A scheduled task or admin function might call this to clean up old notifications.
     *
     * @param olderThan The cutoff date before which read notifications will be deleted.
     * @param userId (Optional) If null, cleans up for all users. Otherwise, for a specific user.
     * @return The number of notifications deleted.
     */
    // int cleanupOldReadNotifications(LocalDateTime olderThan, String userId);

}

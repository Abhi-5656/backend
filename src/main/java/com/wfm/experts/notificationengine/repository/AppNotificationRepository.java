package com.wfm.experts.notificationengine.repository;

import com.wfm.experts.notificationengine.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link AppNotification} entity.
 * This repository will operate within the context of the current tenant's schema.
 */
@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {

    /**
     * Finds all unread in-app notifications for a specific user, ordered by creation date descending.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of unread {@link AppNotification}s.
     */
    Page<AppNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Finds all in-app notifications for a specific user, ordered by creation date descending.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of {@link AppNotification}s.
     */
    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Counts all unread in-app notifications for a specific user.
     *
     * @param userId The ID of the user.
     * @return The count of unread notifications.
     */
    long countByUserIdAndIsReadFalse(String userId);

    /**
     * Finds an in-app notification by its ID and user ID.
     * Useful to ensure a user can only access their own notifications.
     *
     * @param id     The ID of the notification.
     * @param userId The ID of the user.
     * @return An {@link Optional} containing the {@link AppNotification} if found and belongs to the user.
     */
    Optional<AppNotification> findByIdAndUserId(Long id, String userId);

    /**
     * Marks specific notifications as read for a user.
     *
     * @param notificationIds A list of notification IDs to mark as read.
     * @param userId          The ID of the user.
     * @param readAt          The timestamp when the notifications were read.
     * @return The number of notifications updated.
     */
    @Modifying // Indicates that this query will change data
    @Query("UPDATE AppNotification an SET an.isRead = true, an.readAt = :readAt WHERE an.id IN :notificationIds AND an.userId = :userId AND an.isRead = false")
    int markAsRead(@Param("notificationIds") List<Long> notificationIds, @Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Marks all unread notifications for a user as read.
     *
     * @param userId The ID of the user.
     * @param readAt The timestamp when the notifications were read.
     * @return The number of notifications updated.
     */
    @Modifying
    @Query("UPDATE AppNotification an SET an.isRead = true, an.readAt = :readAt WHERE an.userId = :userId AND an.isRead = false")
    int markAllAsReadForUser(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Deletes all read notifications for a specific user that were created before a certain date.
     * Useful for cleanup tasks.
     *
     * @param userId      The ID of the user.
     * @param createdBefore The cutoff date.
     * @return The number of notifications deleted.
     */
    @Modifying
    @Query("DELETE FROM AppNotification an WHERE an.userId = :userId AND an.isRead = true AND an.createdAt < :createdBefore")
    int deleteReadNotificationsOlderThan(@Param("userId") String userId, @Param("createdBefore") LocalDateTime createdBefore);

    /**
     * Deletes expired in-app notifications.
     *
     * @param now The current time, to compare against expiresAt.
     * @return The number of expired notifications deleted.
     */
    @Modifying
    @Query("DELETE FROM AppNotification an WHERE an.expiresAt IS NOT NULL AND an.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Finds an in-app notification by the original notification_request_id.
     * This can be useful for idempotency checks or linking back if needed,
     * though typically in-app notifications are created once per request.
     *
     * @param notificationRequestId The original request ID.
     * @return An {@link Optional} containing the {@link AppNotification}.
     */
    Optional<AppNotification> findByNotificationRequestId(String notificationRequestId);
}

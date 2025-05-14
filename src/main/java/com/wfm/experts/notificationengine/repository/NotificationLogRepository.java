package com.wfm.experts.notificationengine.repository;

import com.wfm.experts.notificationengine.entity.NotificationLog;
import com.wfm.experts.notificationengine.dto.NotificationRequest.ChannelType; // If needed for query methods
import com.wfm.experts.notificationengine.entity.NotificationLog.NotificationStatus; // If needed for query methods
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link NotificationLog} entity.
 * This repository will operate within the context of the current tenant's schema.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Finds a notification log by its unique notificationRequestId.
     *
     * @param notificationRequestId The unique ID of the notification request.
     * @return An {@link Optional} containing the {@link NotificationLog} if found, or empty otherwise.
     */
    Optional<NotificationLog> findByNotificationRequestId(String notificationRequestId);

    /**
     * Finds all notification logs for a specific user.
     *
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s for the given user.
     */
    Page<NotificationLog> findByUserId(String userId, Pageable pageable);

    /**
     * Finds all notification logs for a specific channel.
     *
     * @param channel The notification channel.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s for the given channel.
     */
    Page<NotificationLog> findByChannel(ChannelType channel, Pageable pageable);

    /**
     * Finds all notification logs with a specific status.
     *
     * @param status The notification status.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s with the given status.
     */
    Page<NotificationLog> findByStatus(NotificationStatus status, Pageable pageable);

    /**
     * Finds notification logs created within a specific date range.
     *
     * @param startDate The start date and time of the range.
     * @param endDate The end date and time of the range.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s created within the date range.
     */
    Page<NotificationLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Finds notification logs by recipient address.
     * Useful for searching logs for a specific email, phone number, or device token.
     *
     * @param recipientAddress The recipient's address.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s.
     */
    Page<NotificationLog> findByRecipientAddress(String recipientAddress, Pageable pageable);

    /**
     * Finds notification logs by status and channel.
     *
     * @param status The notification status.
     * @param channel The notification channel.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link NotificationLog}s matching the criteria.
     */
    Page<NotificationLog> findByStatusAndChannel(NotificationStatus status, ChannelType channel, Pageable pageable);

    /**
     * Counts notification logs by status.
     *
     * @param status The notification status.
     * @return The number of logs with the given status.
     */
    long countByStatus(NotificationStatus status);

    /**
     * Counts notification logs by channel and status.
     *
     * @param channel The notification channel.
     * @param status The notification status.
     * @return The number of logs matching the criteria.
     */
    long countByChannelAndStatus(ChannelType channel, NotificationStatus status);

    /**
     * Custom query example: Find logs that are in a FAILED or RETRYING state
     * and were last updated before a certain time (e.g., for a cleanup or re-evaluation job).
     *
     * @param statuses List of statuses to check for.
     * @param updatedBefore The timestamp to compare against.
     * @return A list of {@link NotificationLog}s.
     */
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.status IN :statuses AND nl.updatedAt < :updatedBefore")
    List<NotificationLog> findLogsByStatusInAndUpdatedAtBefore(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("updatedBefore") LocalDateTime updatedBefore
    );

    /**
     * Finds notification logs by provider message ID.
     * This is useful for correlating with status updates from external providers.
     *
     * @param providerMessageId The ID assigned by the external notification provider.
     * @return An {@link Optional} containing the {@link NotificationLog} if found.
     */
    Optional<NotificationLog> findByProviderMessageId(String providerMessageId);

}

package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.repository.AppNotificationRepository;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.tenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AppNotificationServiceImpl implements AppNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AppNotificationServiceImpl.class);

    private final AppNotificationRepository appNotificationRepository;
    private final TemplatingService templatingService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${notification.inapp.default-language:en-US}")
    private String defaultInAppLanguage;

    // Payload keys
    private static final String PAYLOAD_KEY_IN_APP_TITLE = "inAppTitle";
    private static final String PAYLOAD_KEY_IN_APP_BODY = "inAppMessage";
    private static final String PAYLOAD_KEY_ACTION_URL = "actionUrl";
    private static final String PAYLOAD_KEY_ICON_URL = "iconUrl";
    private static final String PAYLOAD_KEY_PRIORITY = "priority";
    private static final String PAYLOAD_KEY_EXPIRES_AT = "expiresAt";
    private static final String PAYLOAD_KEY_ADDITIONAL_DATA = "additionalData";
    static final String BROADCAST_USER_ID_PLACEHOLDER = "BROADCAST_ALL_USERS"; // Special marker for broadcast

    @Autowired
    public AppNotificationServiceImpl(AppNotificationRepository appNotificationRepository,
                                      TemplatingService templatingService,
                                      SimpMessagingTemplate messagingTemplate) {
        this.appNotificationRepository = appNotificationRepository;
        this.templatingService = templatingService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public AppNotification createAppNotification(NotificationRequest notificationRequest) {
        // ... (existing implementation for single user, no changes here)
        String currentTenantId = TenantContext.getTenant();
        logger.info("Creating in-app notification for userId: {}, from requestId: {} within tenant: {}",
                notificationRequest.getUserId(), notificationRequest.getNotificationId(), currentTenantId);

        if (!StringUtils.hasText(notificationRequest.getUserId())) {
            throw new IllegalArgumentException("User ID is required to create an in-app notification.");
        }

        AppNotification appNotification = buildAppNotificationEntity(notificationRequest, notificationRequest.getUserId());

        AppNotification savedNotification = appNotificationRepository.save(appNotification);
        logger.info("In-app notification created with ID: {} for userId: {} in tenant: {}",
                savedNotification.getId(), savedNotification.getUserId(), currentTenantId);

        String userPrincipalName = savedNotification.getUserId();
        String destination = "/queue/in-app-notifications";
        try {
            messagingTemplate.convertAndSendToUser(userPrincipalName, destination, savedNotification);
            logger.info("Successfully sent in-app notification ID {} to user '{}' via WebSocket (destination: /user/{}/{}). Tenant: {}",
                    savedNotification.getId(), userPrincipalName, userPrincipalName, destination, currentTenantId);
        } catch (Exception e) {
            logger.error("Failed to send in-app notification ID {} to user '{}' via WebSocket. Tenant: {}. Error: {}",
                    savedNotification.getId(), userPrincipalName, currentTenantId, e.getMessage(), e);
        }
        return savedNotification;
    }


    @Override
    public AppNotification createAndBroadcastAppNotification(NotificationRequest notificationRequest) {
        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Cannot broadcast notification: Tenant ID is missing from context for request ID: {}", notificationRequest.getNotificationId());
            throw new IllegalStateException("Tenant context is not set, cannot determine broadcast scope.");
        }

        logger.info("Creating and broadcasting in-app notification from requestId: {} within tenant: {}",
                notificationRequest.getNotificationId(), currentTenantId);

        // For broadcasts, the 'userId' in NotificationRequest might be null or ignored.
        // We'll create an AppNotification entity to represent the content.
        // You can decide how to store this:
        // Option 1: Store with a special userId like "BROADCAST"
        // Option 2: Add an isBroadcast flag to AppNotification entity
        // Option 3: Don't store it in the user-specific table if it's purely ephemeral (not recommended for audit)

        // Using Option 1 for this example: Store with a placeholder userId.
        // The actual `notificationRequest.getUserId()` might be null or not relevant for a broadcast.
        AppNotification broadcastContentEntity = buildAppNotificationEntity(notificationRequest, BROADCAST_USER_ID_PLACEHOLDER);
        broadcastContentEntity.setUserId(BROADCAST_USER_ID_PLACEHOLDER); // Mark it as a broadcast

        // Save the representative broadcast message content.
        // This allows viewing the content later, even if it's not tied to individual user history in the same way.
        AppNotification savedBroadcastRepresentative = appNotificationRepository.save(broadcastContentEntity);
        logger.info("Representative broadcast notification content saved with ID: {} for tenant: {}",
                savedBroadcastRepresentative.getId(), currentTenantId);

        // The actual message sent over WebSocket will be this entity or a DTO derived from it.
        // Client will receive this and know it's a broadcast (e.g., by checking userId or a new flag if added).
        String broadcastTopic = "/topic/in-app-notifications/" + currentTenantId;
        try {
            messagingTemplate.convertAndSend(broadcastTopic, savedBroadcastRepresentative);
            logger.info("Successfully broadcast in-app notification (content ID: {}) to topic: {}. Tenant: {}",
                    savedBroadcastRepresentative.getId(), broadcastTopic, currentTenantId);
        } catch (Exception e) {
            logger.error("Failed to broadcast in-app notification (content ID: {}) to topic: {}. Tenant: {}. Error: {}",
                    savedBroadcastRepresentative.getId(), broadcastTopic, currentTenantId, e.getMessage(), e);
            // Decide on error handling.
        }

        return savedBroadcastRepresentative; // Return the representative entity
    }

    // Helper method to build the AppNotification entity from NotificationRequest
    private AppNotification buildAppNotificationEntity(NotificationRequest notificationRequest, String targetUserId) {
        String title;
        String messageBody;
        Map<String, Object> payload = notificationRequest.getPayload() != null ? notificationRequest.getPayload() : Map.of();

        if (StringUtils.hasText(notificationRequest.getTemplateId())) {
            String language = Optional.ofNullable(notificationRequest.getMetadata())
                    .map(meta -> meta.getOrDefault("language", defaultInAppLanguage))
                    .orElse(defaultInAppLanguage);
            TemplatingService.RenderedTemplateContent renderedContent = templatingService.getAndRenderTemplate(
                            notificationRequest.getTemplateId(), NotificationRequest.ChannelType.IN_APP, language, payload)
                    .orElseThrow(() -> new TemplatingService.TemplateProcessingException(
                            "Failed to render in-app template ID: " + notificationRequest.getTemplateId()));
            title = renderedContent.getSubject().orElse(null);
            messageBody = renderedContent.getBody();
        } else {
            title = (String) payload.get(PAYLOAD_KEY_IN_APP_TITLE);
            messageBody = (String) payload.get(PAYLOAD_KEY_IN_APP_BODY);
        }

        title = (String) payload.getOrDefault(PAYLOAD_KEY_IN_APP_TITLE, title);
        messageBody = (String) payload.getOrDefault(PAYLOAD_KEY_IN_APP_BODY, messageBody);

        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title is required for in-app notification.");
        }
        if (!StringUtils.hasText(messageBody)) {
            throw new IllegalArgumentException("Message body is required for in-app notification.");
        }

        AppNotification appNotification = new AppNotification();
        appNotification.setUserId(targetUserId); // Can be specific user or BROADCAST_USER_ID_PLACEHOLDER
        appNotification.setNotificationRequestId(notificationRequest.getNotificationId());
        appNotification.setTitle(title);
        appNotification.setMessageBody(messageBody);

        if (payload.containsKey(PAYLOAD_KEY_ACTION_URL)) {
            appNotification.setActionUrl(String.valueOf(payload.get(PAYLOAD_KEY_ACTION_URL)));
        }
        if (payload.containsKey(PAYLOAD_KEY_ICON_URL)) {
            appNotification.setIconUrl(String.valueOf(payload.get(PAYLOAD_KEY_ICON_URL)));
        }
        if (payload.containsKey(PAYLOAD_KEY_PRIORITY)) {
            try {
                appNotification.setPriority(AppNotification.NotificationPriority.valueOf(String.valueOf(payload.get(PAYLOAD_KEY_PRIORITY)).toUpperCase()));
            } catch (IllegalArgumentException e) { /* Use default */ }
        }
        if (payload.containsKey(PAYLOAD_KEY_EXPIRES_AT)) {
            try {
                appNotification.setExpiresAt(LocalDateTime.parse(String.valueOf(payload.get(PAYLOAD_KEY_EXPIRES_AT))));
            } catch (Exception e) { /* Ignore or log */ }
        }
        if (payload.get(PAYLOAD_KEY_ADDITIONAL_DATA) instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalDataMap = (Map<String, Object>) payload.get(PAYLOAD_KEY_ADDITIONAL_DATA);
            appNotification.setAdditionalData(additionalDataMap);
        }
        return appNotification;
    }


    // ... other existing methods (getUnreadNotificationsForUser, markNotificationAsRead, etc.)
    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching unread in-app notifications for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        return appNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching all in-app notifications for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        return appNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCountForUser(String userId) {
        logger.debug("Fetching unread in-app notification count for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        return appNotificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId) {
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark in-app notification ID: {} as read for userId: {} in tenant: {}", notificationId, userId, currentTenantId);
        Optional<AppNotification> notificationOpt = appNotificationRepository.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty()) {
            logger.warn("AppNotification not found with id: {} for user: {} in tenant: {}", notificationId, userId, currentTenantId);
            return Optional.empty();
        }
        AppNotification notification = notificationOpt.get();
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            AppNotification saved = appNotificationRepository.save(notification);
            logger.info("Marked notification ID: {} as read for userId: {} in tenant: {}", saved.getId(), userId, currentTenantId);
            return Optional.of(saved);
        }
        logger.debug("Notification ID: {} was already read for userId: {} in tenant: {}", notificationId, userId, currentTenantId);
        return Optional.of(notification);
    }

    @Override
    public int markNotificationsAsRead(List<Long> notificationIds, String userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark {} in-app notifications as read for userId: {} in tenant: {}", notificationIds.size(), userId, currentTenantId);
        int updatedCount = appNotificationRepository.markAsRead(notificationIds, userId, LocalDateTime.now());
        logger.info("Marked {} notifications as read for userId: {} in tenant: {}", updatedCount, userId, currentTenantId);
        return updatedCount;
    }

    @Override
    public int markAllNotificationsAsReadForUser(String userId) {
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark all unread in-app notifications as read for userId: {} in tenant: {}", userId, currentTenantId);
        int updatedCount = appNotificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
        logger.info("Marked all ({}) notifications as read for userId: {} in tenant: {}", updatedCount, userId, currentTenantId);
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId) {
        logger.debug("Fetching in-app notification by ID: {} for userId: {} in tenant: {}", notificationId, userId, TenantContext.getTenant());
        return appNotificationRepository.findByIdAndUserId(notificationId, userId);
    }
}
package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.repository.AppNotificationRepository;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.tenancy.TenantContext;
// Import EmployeeRepository if you need to validate user existence or fetch emails for principal names
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
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
import java.util.ArrayList; // For collecting results
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID; // To generate unique request IDs per user if needed

@Service
@Transactional
public class AppNotificationServiceImpl implements AppNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AppNotificationServiceImpl.class);

    private final AppNotificationRepository appNotificationRepository;
    private final TemplatingService templatingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmployeeRepository employeeRepository; // Inject EmployeeRepository

    @Value("${notification.inapp.default-language:en-US}")
    private String defaultInAppLanguage;

    private static final String PAYLOAD_KEY_IN_APP_TITLE = "inAppTitle";
    private static final String PAYLOAD_KEY_IN_APP_BODY = "inAppMessage";
    private static final String PAYLOAD_KEY_ACTION_URL = "actionUrl";
    private static final String PAYLOAD_KEY_ICON_URL = "iconUrl";
    private static final String PAYLOAD_KEY_PRIORITY = "priority";
    private static final String PAYLOAD_KEY_EXPIRES_AT = "expiresAt";
    private static final String PAYLOAD_KEY_ADDITIONAL_DATA = "additionalData";
    static final String BROADCAST_USER_ID_PLACEHOLDER = "BROADCAST_ALL_USERS";

    @Autowired
    public AppNotificationServiceImpl(AppNotificationRepository appNotificationRepository,
                                      TemplatingService templatingService,
                                      SimpMessagingTemplate messagingTemplate,
                                      EmployeeRepository employeeRepository) { // Add EmployeeRepository
        this.appNotificationRepository = appNotificationRepository;
        this.templatingService = templatingService;
        this.messagingTemplate = messagingTemplate;
        this.employeeRepository = employeeRepository; // Initialize
    }

    @Override
    public AppNotification createAppNotification(NotificationRequest notificationRequest) {
        String currentTenantId = TenantContext.getTenant();
        logger.info("Creating in-app notification for userId: {}, from requestId: {} within tenant: {}",
                notificationRequest.getUserId(), notificationRequest.getNotificationId(), currentTenantId);

        if (!StringUtils.hasText(notificationRequest.getUserId())) {
            throw new IllegalArgumentException("User ID is required to create an in-app notification.");
        }

        // Optional: Validate if the userId exists in the employee table for the current tenant
        // This depends on whether notificationRequest.getUserId() is the employeeId or email.
        // Assuming notificationRequest.getUserId() is the identifier used as the STOMP principal name.
        // If it's an employeeId, you might fetch the employee to get their email if email is the principal.
        // For simplicity, we assume notificationRequest.getUserId() is what we need for convertAndSendToUser.

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

        AppNotification broadcastContentEntity = buildAppNotificationEntity(notificationRequest, BROADCAST_USER_ID_PLACEHOLDER);
        AppNotification savedBroadcastRepresentative = appNotificationRepository.save(broadcastContentEntity);
        logger.info("Representative broadcast notification content saved with ID: {} for tenant: {}",
                savedBroadcastRepresentative.getId(), currentTenantId);

        String broadcastTopic = "/topic/in-app-notifications/" + currentTenantId;
        try {
            messagingTemplate.convertAndSend(broadcastTopic, savedBroadcastRepresentative);
            logger.info("Successfully broadcast in-app notification (content ID: {}) to topic: {}. Tenant: {}",
                    savedBroadcastRepresentative.getId(), broadcastTopic, currentTenantId);
        } catch (Exception e) {
            logger.error("Failed to broadcast in-app notification (content ID: {}) to topic: {}. Tenant: {}. Error: {}",
                    savedBroadcastRepresentative.getId(), broadcastTopic, currentTenantId, e.getMessage(), e);
        }
        return savedBroadcastRepresentative;
    }

    @Override
    public List<AppNotification> createAppNotificationsForSpecificUsers(NotificationRequest baseNotificationRequest, List<String> targetUserIds) {
        String currentTenantId = TenantContext.getTenant();
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Cannot send targeted notifications: Tenant ID is missing from context for base request ID: {}", baseNotificationRequest.getNotificationId());
            throw new IllegalStateException("Tenant context is not set, cannot determine send scope.");
        }
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            logger.warn("No target user IDs provided for specific user notification. Base request ID: {}", baseNotificationRequest.getNotificationId());
            return new ArrayList<>();
        }

        logger.info("Processing in-app notification for {} specific users. Base request ID: {}, Tenant: {}",
                targetUserIds.size(), baseNotificationRequest.getNotificationId(), currentTenantId);

        List<AppNotification> createdNotifications = new ArrayList<>();

        for (String targetUserId : targetUserIds) {
            if (!StringUtils.hasText(targetUserId)) {
                logger.warn("Skipping empty or null targetUserId in the list for base request ID: {}", baseNotificationRequest.getNotificationId());
                continue;
            }

            // Optional: Validate user existence in the current tenant.
            // Assumes targetUserId is the Employee's unique ID (e.g., employeeId from Employee entity)
            // or the email if that's what you use as the STOMP principal name.
            // If targetUserId is an email, use findByEmail. If it's employeeId, use findByEmployeeId.
            // Ensure your Employee entity and repository have a method for this.
            // For this example, let's assume targetUserId is what's needed for STOMP principal.
            // If you need to fetch the employee to confirm existence:
            // Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(targetUserId); // Or findByEmail
            // if (employeeOpt.isEmpty()) {
            //     logger.warn("Employee with ID/Email '{}' not found in tenant '{}'. Skipping notification. Base request ID: {}",
            //             targetUserId, currentTenantId, baseNotificationRequest.getNotificationId());
            //     continue;
            // }
            // String userPrincipalNameForStomp = employeeOpt.get().getEmail(); // Or whatever field is the STOMP principal

            // Create a unique notification request ID for each user-specific notification for better tracking in logs
            String userSpecificRequestId = baseNotificationRequest.getNotificationId() + "-" + targetUserId + "-" + UUID.randomUUID().toString().substring(0, 8);

            AppNotification appNotification = buildAppNotificationEntity(
                    new NotificationRequest( // Create a new request to avoid modifying the base one if it's reused
                            userSpecificRequestId,
                            targetUserId, // This is crucial for the individual notification
                            baseNotificationRequest.getChannel(),
                            null, // recipientAddress not used for IN_APP
                            baseNotificationRequest.getTemplateId(),
                            baseNotificationRequest.getPayload(),
                            baseNotificationRequest.getMetadata()
                    ),
                    targetUserId // Pass targetUserId to buildAppNotificationEntity
            );

            try {
                AppNotification savedNotification = appNotificationRepository.save(appNotification);
                createdNotifications.add(savedNotification);
                logger.info("In-app notification created with ID: {} for specific user: {} in tenant: {}",
                        savedNotification.getId(), targetUserId, currentTenantId);

                // Send to this specific user via WebSocket
                // Ensure `targetUserId` here matches the STOMP principal name format (e.g., email or employeeId)
                String userPrincipalName = targetUserId; // Assuming targetUserId IS the STOMP principal name
                String destination = "/queue/in-app-notifications";
                messagingTemplate.convertAndSendToUser(userPrincipalName, destination, savedNotification);
                logger.info("Successfully sent in-app notification ID {} to specific user '{}' via WebSocket. Tenant: {}",
                        savedNotification.getId(), userPrincipalName, currentTenantId);

            } catch (Exception e) {
                logger.error("Failed to create or send in-app notification for specific user '{}'. Base request ID: {}. Tenant: {}. Error: {}",
                        targetUserId, baseNotificationRequest.getNotificationId(), currentTenantId, e.getMessage(), e);
                // Decide on error handling: continue with others, or stop?
            }
        }
        return createdNotifications;
    }


    private AppNotification buildAppNotificationEntity(NotificationRequest notificationRequest, String targetUserId) {
        // ... (this helper method remains the same)
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
        // Ensure the targetUserId (which could be a specific user or the BROADCAST_PLACEHOLDER) is set
        appNotification.setUserId(targetUserId);
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

    // ... other existing methods ...
    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching unread in-app notifications for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        // Filter out broadcast messages if necessary when fetching for a specific user
        return appNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching all in-app notifications for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        // Filter out broadcast messages if necessary
        return appNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCountForUser(String userId) {
        logger.debug("Fetching unread in-app notification count for userId: {} in tenant: {}", userId, TenantContext.getTenant());
        // Filter out broadcast messages if necessary
        return appNotificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    @Override
    public Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId) {
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark in-app notification ID: {} as read for userId: {} in tenant: {}", notificationId, userId, currentTenantId);
        // Ensure we don't mark broadcast representative messages as read by a specific user,
        // unless that's desired and the ID matches a user-specific copy.
        // The findByIdAndUserId should handle this correctly if userId for broadcast is BROADCAST_USER_ID_PLACEHOLDER
        Optional<AppNotification> notificationOpt = appNotificationRepository.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty() || BROADCAST_USER_ID_PLACEHOLDER.equals(notificationOpt.get().getUserId())) {
            logger.warn("AppNotification not found for user or it's a broadcast representative: ID {}, User {}, Tenant: {}", notificationId, userId, currentTenantId);
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
        // This repository method already filters by userId.
        int updatedCount = appNotificationRepository.markAsRead(notificationIds, userId, LocalDateTime.now());
        logger.info("Marked {} notifications as read for userId: {} in tenant: {}", updatedCount, userId, currentTenantId);
        return updatedCount;
    }

    @Override
    public int markAllNotificationsAsReadForUser(String userId) {
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark all unread in-app notifications as read for userId: {} in tenant: {}", userId, currentTenantId);
        // This repository method already filters by userId.
        int updatedCount = appNotificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
        logger.info("Marked all ({}) notifications as read for userId: {} in tenant: {}", updatedCount, userId, currentTenantId);
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId) {
        logger.debug("Fetching in-app notification by ID: {} for userId: {} in tenant: {}", notificationId, userId, TenantContext.getTenant());
        // Ensure broadcast placeholder isn't fetched if a specific user ID is expected to not be the placeholder.
        Optional<AppNotification> notification = appNotificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification.isPresent() && BROADCAST_USER_ID_PLACEHOLDER.equals(notification.get().getUserId()) && !BROADCAST_USER_ID_PLACEHOLDER.equals(userId)) {
            // If user is asking for a specific ID that happens to be a broadcast placeholder, but their own ID is different.
            // This case should ideally not happen if IDs are unique.
            return Optional.empty();
        }
        return notification;
    }
}
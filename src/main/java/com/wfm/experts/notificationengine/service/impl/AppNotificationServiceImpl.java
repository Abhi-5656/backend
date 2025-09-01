package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.repository.AppNotificationRepository;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.notificationengine.service.TemplatingService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy; // For async self-injection
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async; // Import Async
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture; // For async result


@Service
// Transactional at class level might be okay, but for async methods, be careful.
// Individual async methods should often manage their own transactions if they do DB work.
public class AppNotificationServiceImpl implements AppNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AppNotificationServiceImpl.class);

    private final AppNotificationRepository appNotificationRepository;
    private final TemplatingService templatingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmployeeRepository employeeRepository;

    // For self-injection to call @Async methods from within the same class
    private AppNotificationService self;

    @Autowired
    public void setSelf(@Lazy AppNotificationService self) {
        this.self = self;
    }


    @Value("${notification.inapp.default-language:en-US}")
    private String defaultInAppLanguage;

    // ... (payload keys and BROADCAST_USER_ID_PLACEHOLDER remain the same) ...
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
                                      EmployeeRepository employeeRepository) {
        this.appNotificationRepository = appNotificationRepository;
        this.templatingService = templatingService;
        this.messagingTemplate = messagingTemplate;
        this.employeeRepository = employeeRepository;
    }

    // --- createAppNotification and createAndBroadcastAppNotification remain the same ---
    // They are already efficient for their specific purposes (single user, single topic broadcast)
    @Override
    @Transactional // This transaction is for a single user notification
    public AppNotification createAppNotification(NotificationRequest notificationRequest) {
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
    @Transactional // This transaction is for the representative broadcast entity
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

    // --- OPTIMIZED METHOD for sending to a list of specific users ---
    @Override
    // This outer method should NOT be @Transactional if the inner work is async and has its own transactions.
    // Or, if it is, be aware of the implications for the async calls.
    // For simplicity, let's make the async helper transactional.
    public List<AppNotification> createAppNotificationsForSpecificUsers(
            NotificationRequest baseNotificationRequest, List<String> targetUserIds) {
        String currentTenantId = TenantContext.getTenant(); // Capture tenant from the calling (admin's HTTP) thread
        if (currentTenantId == null || currentTenantId.trim().isEmpty()) {
            logger.error("Cannot send targeted notifications: Tenant ID is missing from context. Base request ID: {}", baseNotificationRequest.getNotificationId());
            throw new IllegalStateException("Tenant context is not set.");
        }
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            logger.warn("No target user IDs provided. Base request ID: {}", baseNotificationRequest.getNotificationId());
            return new ArrayList<>();
        }

        logger.info("Initiating sending in-app notifications to {} specific users. Base request ID: {}, Tenant: {}",
                targetUserIds.size(), baseNotificationRequest.getNotificationId(), currentTenantId);

        // Build the common content ONCE
        // The 'userId' in this initial build is just a template; it will be overridden for each actual user.
        AppNotification commonContentNotification = buildAppNotificationEntity(baseNotificationRequest, "TEMPLATE_USER");

        List<CompletableFuture<AppNotification>> futures = new ArrayList<>();
        for (String targetUserId : targetUserIds) {
            if (!StringUtils.hasText(targetUserId)) {
                logger.warn("Skipping empty/null targetUserId for base request ID: {}", baseNotificationRequest.getNotificationId());
                continue;
            }
            // Call the @Async helper method
            // Pass currentTenantId explicitly because TenantContext is thread-local and won't propagate to @Async threads by default
            futures.add(self.processAndSendToSingleUserAsync(commonContentNotification, targetUserId, currentTenantId, baseNotificationRequest.getNotificationId()));
        }

        // Wait for all async tasks to complete and collect results (optional, depending on desired behavior)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Wait for all

        List<AppNotification> successfullySentNotifications = new ArrayList<>();
        int failureCount = 0;
        for (CompletableFuture<AppNotification> future : futures) {
            try {
                AppNotification result = future.getNow(null); // Get result, null if exception
                if (result != null) {
                    successfullySentNotifications.add(result);
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
                logger.warn("An async notification task failed: {}", e.getMessage());
            }
        }
        logger.info("Finished processing targeted notifications for base request ID {}. Success: {}, Failures: {}",
                baseNotificationRequest.getNotificationId(), successfullySentNotifications.size(), failureCount);
        return successfullySentNotifications;
    }

    @Async // Make sure @EnableAsync is on a @Configuration class
    public CompletableFuture<AppNotification> processAndSendToSingleUserAsync(
            AppNotification commonContent, String targetUserId, String tenantId, String baseNotificationRequestId) {

        TenantContext.setTenant(tenantId); // SET TenantContext for this async thread
        try {
            // Optional: User existence check (EmployeeRepository)
            // if (!employeeRepository.existsByEmployeeIdAndTenantId(targetUserId, tenantId)) { // Example
            //     logger.warn("User {} not found in tenant {}. Skipping.", targetUserId, tenantId);
            //     return CompletableFuture.completedFuture(null); // Indicate failure or skip
            // }

            AppNotification userSpecificNotification = new AppNotification();
            // Copy content from commonContent
            userSpecificNotification.setTitle(commonContent.getTitle());
            userSpecificNotification.setMessageBody(commonContent.getMessageBody());
            userSpecificNotification.setActionUrl(commonContent.getActionUrl());
            userSpecificNotification.setIconUrl(commonContent.getIconUrl());
            userSpecificNotification.setPriority(commonContent.getPriority());
            userSpecificNotification.setExpiresAt(commonContent.getExpiresAt());
            userSpecificNotification.setAdditionalData(commonContent.getAdditionalData() != null ? new HashMap<>(commonContent.getAdditionalData()) : null);

            // Set user-specific details
            userSpecificNotification.setUserId(targetUserId);
            userSpecificNotification.setNotificationRequestId(baseNotificationRequestId + "-" + targetUserId + "-" + UUID.randomUUID().toString().substring(0,4)); // Unique request ID

            AppNotification savedNotification = appNotificationRepository.save(userSpecificNotification);
            // logger.debug("Saved notification for user {} (ID: {}) in tenant {}", targetUserId, savedNotification.getId(), tenantId);


            String userPrincipalName = targetUserId; // Assuming targetUserId is the STOMP principal name
            String destination = "/queue/in-app-notifications";
            messagingTemplate.convertAndSendToUser(userPrincipalName, destination, savedNotification);
            logger.info("Async: Sent to user {} (Notif ID {}) in tenant {}", userPrincipalName, savedNotification.getId(), tenantId);

            return CompletableFuture.completedFuture(savedNotification);
        } catch (Exception e) {
            logger.error("Async: Failed for user {} in tenant {}: {}", targetUserId, tenantId, e.getMessage(), e);
            return CompletableFuture.completedFuture(null); // Indicate failure
        } finally {
            TenantContext.clear(); // CLEAR TenantContext for this async thread
        }
    }


    // Helper method buildAppNotificationEntity (remains mostly the same)
    private AppNotification buildAppNotificationEntity(NotificationRequest notificationRequest, String targetUserId) {
        // ... (implementation as before)
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

    // ... (other existing methods: getUnreadNotificationsForUser, etc.) ...
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
        // ... (remains the same) ...
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
        // ... (remains the same) ...
        String currentTenantId = TenantContext.getTenant();
        logger.info("Attempting to mark all unread in-app notifications as read for userId: {} in tenant: {}", userId, currentTenantId);
        int updatedCount = appNotificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
        logger.info("Marked all ({}) notifications as read for userId: {} in tenant: {}", updatedCount, userId, currentTenantId);
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId) {
        // ... (remains the same, with consideration for BROADCAST_USER_ID_PLACEHOLDER) ...
        logger.debug("Fetching in-app notification by ID: {} for userId: {} in tenant: {}", notificationId, userId, TenantContext.getTenant());
        Optional<AppNotification> notification = appNotificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification.isPresent() && BROADCAST_USER_ID_PLACEHOLDER.equals(notification.get().getUserId()) && !BROADCAST_USER_ID_PLACEHOLDER.equals(userId)) {
            return Optional.empty();
        }
        return notification;
    }
}
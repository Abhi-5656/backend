package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.AppNotification;
import com.wfm.experts.notificationengine.exception.AppNotificationNotFoundException; // Assuming you'll create this
import com.wfm.experts.notificationengine.exception.UnauthorizedNotificationAccessException; // Assuming you'll create this
import com.wfm.experts.notificationengine.repository.AppNotificationRepository;
import com.wfm.experts.notificationengine.service.AppNotificationService;
import com.wfm.experts.notificationengine.service.TemplatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link AppNotificationService}.
 * Manages CRUD and status operations for AppNotifications.
 */
@Service
@Transactional // Apply transactionality to all public methods by default
public class AppNotificationServiceImpl implements AppNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AppNotificationServiceImpl.class);

    private final AppNotificationRepository appNotificationRepository;
    private final TemplatingService templatingService;

    @Value("${notification.inapp.default-language:en-US}") // Default language for in-app templates
    private String defaultInAppLanguage;

    // Keys to look for in the NotificationRequest payload for direct in-app content
    private static final String PAYLOAD_KEY_IN_APP_TITLE = "inAppTitle";
    private static final String PAYLOAD_KEY_IN_APP_BODY = "inAppMessage"; // Or inAppBody
    private static final String PAYLOAD_KEY_ACTION_URL = "actionUrl";
    private static final String PAYLOAD_KEY_ICON_URL = "iconUrl";
    private static final String PAYLOAD_KEY_PRIORITY = "priority";
    private static final String PAYLOAD_KEY_EXPIRES_AT = "expiresAt"; // Expect ISO DateTime string
    private static final String PAYLOAD_KEY_ADDITIONAL_DATA = "additionalData"; // Expect Map<String, Object>


    @Autowired
    public AppNotificationServiceImpl(AppNotificationRepository appNotificationRepository,
                                      TemplatingService templatingService) {
        this.appNotificationRepository = appNotificationRepository;
        this.templatingService = templatingService;
    }

    @Override
    public AppNotification createAppNotification(NotificationRequest notificationRequest) {
        logger.info("Creating in-app notification for userId: {}, from requestId: {}",
                notificationRequest.getUserId(), notificationRequest.getNotificationId());

        if (!StringUtils.hasText(notificationRequest.getUserId())) {
            throw new IllegalArgumentException("User ID is required to create an in-app notification.");
        }

        String title;
        String messageBody;
        Map<String, Object> payload = notificationRequest.getPayload() != null ? notificationRequest.getPayload() : Map.of();

        // Attempt to render from template if templateId is provided
        if (StringUtils.hasText(notificationRequest.getTemplateId())) {
            String language = Optional.ofNullable(notificationRequest.getMetadata())
                    .map(meta -> meta.getOrDefault("language", defaultInAppLanguage))
                    .orElse(defaultInAppLanguage);

            TemplatingService.RenderedTemplateContent renderedContent = templatingService.getAndRenderTemplate(
                    notificationRequest.getTemplateId(),
                    NotificationRequest.ChannelType.IN_APP, // Assuming you add IN_APP to ChannelType enum
                    language,
                    payload
            ).orElseThrow(() -> new TemplatingService.TemplateProcessingException(
                    "Failed to render in-app template ID: " + notificationRequest.getTemplateId()));

            title = renderedContent.getSubject().orElse(null); // Template subject might map to title
            messageBody = renderedContent.getBody();
        } else {
            // Fallback to direct payload if no template
            title = (String) payload.get(PAYLOAD_KEY_IN_APP_TITLE);
            messageBody = (String) payload.get(PAYLOAD_KEY_IN_APP_BODY);
        }

        // Allow payload to override template-rendered title/body
        title = (String) payload.getOrDefault(PAYLOAD_KEY_IN_APP_TITLE, title);
        messageBody = (String) payload.getOrDefault(PAYLOAD_KEY_IN_APP_BODY, messageBody);


        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title is required for in-app notification.");
        }
        if (!StringUtils.hasText(messageBody)) {
            throw new IllegalArgumentException("Message body is required for in-app notification.");
        }

        AppNotification appNotification = new AppNotification();
        appNotification.setUserId(notificationRequest.getUserId());
        appNotification.setNotificationRequestId(notificationRequest.getNotificationId());
        appNotification.setTitle(title);
        appNotification.setMessageBody(messageBody);

        // Extract optional fields from payload
        if (payload.containsKey(PAYLOAD_KEY_ACTION_URL)) {
            appNotification.setActionUrl(String.valueOf(payload.get(PAYLOAD_KEY_ACTION_URL)));
        }
        if (payload.containsKey(PAYLOAD_KEY_ICON_URL)) {
            appNotification.setIconUrl(String.valueOf(payload.get(PAYLOAD_KEY_ICON_URL)));
        }
        if (payload.containsKey(PAYLOAD_KEY_PRIORITY)) {
            try {
                appNotification.setPriority(AppNotification.NotificationPriority.valueOf(String.valueOf(payload.get(PAYLOAD_KEY_PRIORITY)).toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid priority value in payload: {}. Using default.", payload.get(PAYLOAD_KEY_PRIORITY));
            }
        }
        if (payload.containsKey(PAYLOAD_KEY_EXPIRES_AT)) {
            try {
                appNotification.setExpiresAt(LocalDateTime.parse(String.valueOf(payload.get(PAYLOAD_KEY_EXPIRES_AT))));
            } catch (Exception e) {
                logger.warn("Invalid expiresAt value in payload: {}. Not setting expiration.", payload.get(PAYLOAD_KEY_EXPIRES_AT));
            }
        }
        if (payload.get(PAYLOAD_KEY_ADDITIONAL_DATA) instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalDataMap = (Map<String, Object>) payload.get(PAYLOAD_KEY_ADDITIONAL_DATA);
            appNotification.setAdditionalData(additionalDataMap);
        }


        // isRead defaults to false, createdAt by @PrePersist
        AppNotification savedNotification = appNotificationRepository.save(appNotification);
        logger.info("In-app notification created with ID: {} for userId: {}", savedNotification.getId(), savedNotification.getUserId());
        return savedNotification;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getUnreadNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching unread in-app notifications for userId: {}", userId);
        return appNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppNotification> getAllNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Fetching all in-app notifications for userId: {}", userId);
        return appNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCountForUser(String userId) {
        logger.debug("Fetching unread in-app notification count for userId: {}", userId);
        return appNotificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Optional<AppNotification> markNotificationAsRead(Long notificationId, String userId) {
        logger.info("Attempting to mark in-app notification ID: {} as read for userId: {}", notificationId, userId);
        Optional<AppNotification> notificationOpt = appNotificationRepository.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty()) {
            throw new AppNotificationNotFoundException("AppNotification not found with id: " + notificationId + " for user: " + userId);
        }
        AppNotification notification = notificationOpt.get();
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return Optional.of(appNotificationRepository.save(notification));
        }
        logger.debug("Notification ID: {} was already read for userId: {}", notificationId, userId);
        return Optional.of(notification); // Return the already read notification
    }

    @Override
    public int markNotificationsAsRead(List<Long> notificationIds, String userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        logger.info("Attempting to mark {} in-app notifications as read for userId: {}", notificationIds.size(), userId);
        return appNotificationRepository.markAsRead(notificationIds, userId, LocalDateTime.now());
    }

    @Override
    public int markAllNotificationsAsReadForUser(String userId) {
        logger.info("Attempting to mark all unread in-app notifications as read for userId: {}", userId);
        return appNotificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppNotification> getNotificationByIdAndUser(Long notificationId, String userId) {
        logger.debug("Fetching in-app notification by ID: {} for userId: {}", notificationId, userId);
        return appNotificationRepository.findByIdAndUserId(notificationId, userId);
    }
}

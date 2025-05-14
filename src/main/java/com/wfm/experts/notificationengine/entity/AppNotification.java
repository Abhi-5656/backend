package com.wfm.experts.notificationengine.entity;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // For ChannelType if needed, though it's implicitly IN_APP
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * JPA Entity representing an In-App Notification to be displayed to a user
 * within the application.
 * This entity will be stored in a tenant-specific schema.
 */
@Entity
@Table(name = "app_notifications") // Renamed table name
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "User ID cannot be blank")
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId; // The user who should see this notification

    @Column(name = "notification_request_id", nullable = false, unique = true, updatable = false)
    private String notificationRequestId; // Link back to the original NotificationLog/Request

    @NotBlank(message = "Title cannot be blank")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Message body cannot be blank")
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; // Has the user read this notification?

    @Column(name = "read_at")
    private LocalDateTime readAt; // When the user read it

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at") // Optional: if in-app notifications should auto-expire
    private LocalDateTime expiresAt;

    @Column(name = "action_url") // Optional: A URL/deep-link to navigate to when the notification is clicked
    private String actionUrl;

    @Column(name = "icon_url") // Optional: URL for an icon to display with the notification
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20) // Optional: LOW, MEDIUM, HIGH
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_data", columnDefinition = "jsonb") // For any extra structured data
    private Map<String, Object> additionalData;


    public enum NotificationPriority {
        LOW, MEDIUM, HIGH
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public AppNotification() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationRequestId() {
        return notificationRequestId;
    }

    public void setNotificationRequestId(String notificationRequestId) {
        this.notificationRequestId = notificationRequestId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppNotification that = (AppNotification) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(notificationRequestId, that.notificationRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, notificationRequestId);
    }

    @Override
    public String toString() {
        return "AppNotification{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", notificationRequestId='" + notificationRequestId + '\'' +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}

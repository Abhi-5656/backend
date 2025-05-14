package com.wfm.experts.notificationengine.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

/**
 * Data Transfer Object representing a request to send a notification.
 * This object is typically created by a service wanting to send a notification
 * and is then published to a RabbitMQ queue.
 * It does NOT contain tenantId as that will be passed via message headers.
 */
public class NotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L; // Keep or update as per your serialization strategy

    private String notificationId;      // Unique ID for this specific notification instance
    private String userId;              // ID of the user to whom the notification is intended
    private ChannelType channel;        // Enum indicating the target channel (EMAIL, PUSH_FCM, PUSH_APNS, IN_APP)
    private String recipientAddress;    // The actual address for the channel (e.g., email, phone, device token, or null for IN_APP if userId is primary target)
    private String templateId;          // Identifier for the notification template to be used
    private Map<String, Object> payload; // Key-value pairs for template variables and other channel-specific data
    private Map<String, String> metadata; // Additional metadata (e.g., source service)

    /**
     * Enum to define the supported notification channels.
     */
    public enum ChannelType {
        EMAIL,
        // SMS, // SMS is on hold
        PUSH_FCM,
        PUSH_APNS,
        IN_APP // Added for In-App Notifications
    }

    // Constructors
    public NotificationRequest() {
        // Generate a default unique ID if not provided
        this.notificationId = UUID.randomUUID().toString();
    }

    /**
     * Constructor for basic notification request.
     *
     * @param userId           ID of the target user.
     * @param channel          The notification channel.
     * @param recipientAddress The specific address for the channel (e.g., email, device token).
     * For IN_APP, this might be null if userId is sufficient.
     * @param templateId       The ID of the template to use.
     * @param payload          Data for template rendering and channel-specific needs.
     */
    public NotificationRequest(String userId, ChannelType channel, String recipientAddress, String templateId, Map<String, Object> payload) {
        this(); // Calls the default constructor to set notificationId
        this.userId = userId;
        this.channel = channel;
        this.recipientAddress = recipientAddress;
        this.templateId = templateId;
        this.payload = payload;
    }

    /**
     * Full constructor.
     *
     * @param notificationId   A specific notification ID (if not using the auto-generated one).
     * @param userId           ID of the target user.
     * @param channel          The notification channel.
     * @param recipientAddress The specific address for the channel.
     * @param templateId       The ID of the template to use.
     * @param payload          Data for template rendering.
     * @param metadata         Additional metadata.
     */
    public NotificationRequest(String notificationId, String userId, ChannelType channel, String recipientAddress, String templateId, Map<String, Object> payload, Map<String, String> metadata) {
        this.notificationId = (notificationId != null && !notificationId.trim().isEmpty()) ? notificationId : UUID.randomUUID().toString();
        this.userId = userId;
        this.channel = channel;
        this.recipientAddress = recipientAddress;
        this.templateId = templateId;
        this.payload = payload;
        this.metadata = metadata;
    }

    // Getters and Setters

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    // toString() for logging and debugging
    @Override
    public String toString() {
        return "NotificationRequest{" +
                "notificationId='" + notificationId + '\'' +
                ", userId='" + userId + '\'' +
                ", channel=" + channel +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", templateId='" + templateId + '\'' +
                ", payload=" + payload +
                ", metadata=" + metadata +
                '}';
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationRequest that = (NotificationRequest) o;
        return Objects.equals(getNotificationId(), that.getNotificationId()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                getChannel() == that.getChannel() &&
                Objects.equals(getRecipientAddress(), that.getRecipientAddress()) &&
                Objects.equals(getTemplateId(), that.getTemplateId()) &&
                Objects.equals(getPayload(), that.getPayload()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNotificationId(), getUserId(), getChannel(), getRecipientAddress(), getTemplateId(), getPayload(), getMetadata());
    }
}

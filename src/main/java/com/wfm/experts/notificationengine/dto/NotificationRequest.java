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

    private static final long serialVersionUID = 1L; // Original serialVersionUID

    private String notificationId;
    private String userId;
    private ChannelType channel;
    private String recipientAddress;
    private String templateId;
    private Map<String, Object> payload;
    private Map<String, String> metadata;

    public enum ChannelType {
        EMAIL,
        // SMS, // Assuming SMS is still on hold
        PUSH_FCM,
        PUSH_APNS
    }

    public NotificationRequest() {
        this.notificationId = UUID.randomUUID().toString();
    }

    public NotificationRequest(String userId, ChannelType channel, String recipientAddress, String templateId, Map<String, Object> payload) {
        this();
        this.userId = userId;
        this.channel = channel;
        this.recipientAddress = recipientAddress;
        this.templateId = templateId;
        this.payload = payload;
    }

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

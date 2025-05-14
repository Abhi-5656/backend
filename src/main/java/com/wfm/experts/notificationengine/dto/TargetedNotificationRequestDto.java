package com.wfm.experts.notificationengine.dto; // Correct package

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TargetedNotificationRequestDto {

    @NotNull(message = "Notification content cannot be null.")
    @Valid // Ensures nested validation on NotificationRequest fields
    private NotificationRequest notificationContent;

    @NotEmpty(message = "Target user IDs cannot be empty.")
    private List<String> targetUserIds;

    // Getters and Setters
    public NotificationRequest getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(NotificationRequest notificationContent) {
        this.notificationContent = notificationContent;
    }

    public List<String> getTargetUserIds() {
        return targetUserIds;
    }

    public void setTargetUserIds(List<String> targetUserIds) {
        this.targetUserIds = targetUserIds;
    }

    // toString, equals, hashCode (optional, but good practice)
    @Override
    public String toString() {
        return "TargetedNotificationRequestDto{" +
                "notificationContent=" + (notificationContent != null ? notificationContent.getNotificationId() : "null") +
                ", targetUserIdsCount=" + (targetUserIds != null ? targetUserIds.size() : 0) +
                '}';
    }
}
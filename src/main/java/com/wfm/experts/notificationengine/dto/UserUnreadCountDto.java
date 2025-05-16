package com.wfm.experts.notificationengine.dto; // Or your DTO package

public class UserUnreadCountDto {
    private String userId;
    private long unreadCount;

    public UserUnreadCountDto(String userId, long unreadCount) {
        this.userId = userId;
        this.unreadCount = unreadCount;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    // Setters (optional, depending on usage)
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
package com.wfm.experts.notificationengine.dto;

import com.wfm.experts.notificationengine.dto.NotificationRequest.ChannelType; // Reusing from existing DTO
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for creating, updating, and displaying NotificationTemplates.
 */
public class NotificationTemplateDto {

    private Long id; // Present for responses, null for creation

    @NotBlank(message = "Template ID cannot be blank")
    @Size(max = 100, message = "Template ID must be less than 100 characters")
    private String templateId;

    @NotNull(message = "Channel type cannot be null")
    private ChannelType channel;

    @NotBlank(message = "Language code cannot be blank")
    @Size(max = 10, message = "Language code must be less than 10 characters")
    private String language;

    @NotNull(message = "Version cannot be null")
    @Positive(message = "Version must be a positive number")
    private Integer version = 1; // Default to 1 for creation if not specified

    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject; // Nullable, mainly for emails

    @NotBlank(message = "Template body cannot be blank")
    private String body; // TEXT

    private boolean isActive = true; // Default to true

    private LocalDateTime createdAt; // For display
    private LocalDateTime updatedAt; // For display

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    // Constructors
    public NotificationTemplateDto() {
    }

    public NotificationTemplateDto(Long id, String templateId, ChannelType channel, String language, Integer version, String subject, String body, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, String description) {
        this.id = id;
        this.templateId = templateId;
        this.channel = channel;
        this.language = language;
        this.version = version;
        this.subject = subject;
        this.body = body;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NotificationTemplateDto{" +
                "id=" + id +
                ", templateId='" + templateId + '\'' +
                ", channel=" + channel +
                ", language='" + language + '\'' +
                ", version=" + version +
                ", subject='" + subject + '\'' +
                ", isActive=" + isActive +
                ", description='" + description + '\'' +
                '}';
    }
}

package com.wfm.experts.notificationengine.entity;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // Reusing ChannelType
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing a notification template.
 * Templates are used to generate the content of notifications for different channels.
 * This entity will be stored in a tenant-specific schema.
 */
@Entity
@Table(name = "notification_templates",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"template_id", "language", "version"}, name = "uk_template_id_lang_version")
        }
)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Template ID cannot be blank")
    @Size(max = 100, message = "Template ID must be less than 100 characters")
    @Column(name = "template_id", nullable = false, length = 100)
    private String templateId; // e.g., "welcome_email", "order_confirmation_sms"

    @NotNull(message = "Channel type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 50)
    private NotificationRequest.ChannelType channel;

    @NotBlank(message = "Language code cannot be blank")
    @Size(max = 10, message = "Language code must be less than 10 characters")
    @Column(name = "language", nullable = false, length = 10) // e.g., "en-US", "fr-FR"
    private String language;

    @Column(name = "version", nullable = false)
    private int version = 1; // Version of the template

    @Size(max = 255, message = "Subject must be less than 255 characters")
    @Column(name = "subject", length = 255) // Subject line, primarily for email templates
    private String subject; // Can contain placeholders like {{userName}}

    @NotBlank(message = "Template body cannot be blank")
    @Lob // For potentially large template content
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body; // Template content with placeholders, e.g., "Hello {{userName}}, your order {{orderId}} is confirmed."

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // To enable/disable the template

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Size(max = 255, message = "Description must be less than 255 characters")
    @Column(name = "description")
    private String description; // Optional description of the template's purpose

    // For multi-tenant setups where templates might be shared or specific,
    // but in your schema-per-tenant model, this is implicitly tenant-specific.

    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public NotificationTemplate() {
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

    public NotificationRequest.ChannelType getChannel() {
        return channel;
    }

    public void setChannel(NotificationRequest.ChannelType channel) {
        this.channel = channel;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTemplate that = (NotificationTemplate) o;
        return getVersion() == that.getVersion() &&
                isActive() == that.isActive() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTemplateId(), that.getTemplateId()) &&
                getChannel() == that.getChannel() &&
                Objects.equals(getLanguage(), that.getLanguage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTemplateId(), getChannel(), getLanguage(), getVersion(), isActive());
    }

    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id=" + id +
                ", templateId='" + templateId + '\'' +
                ", channel=" + channel +
                ", language='" + language + '\'' +
                ", version=" + version +
                ", subject='" + subject + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

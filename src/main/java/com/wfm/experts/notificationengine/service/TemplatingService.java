package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // For ChannelType and payload
import com.wfm.experts.notificationengine.entity.NotificationTemplate; // If a method returns the raw template
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing and processing notification templates.
 * This service is responsible for fetching templates from a persistent store
 * (e.g., database via NotificationTemplateRepository) and rendering them
 * with dynamic data.
 */
public interface TemplatingService {

    /**
     * Represents the processed content of a notification, potentially including
     * a subject (for emails) and a body.
     */
    class RenderedTemplateContent {
        private final String subject; // Nullable, mainly for emails
        private final String body;

        public RenderedTemplateContent(String body) {
            this(null, body);
        }

        public RenderedTemplateContent(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }

        public Optional<String> getSubject() {
            return Optional.ofNullable(subject);
        }

        public String getBody() {
            return body;
        }
    }

    /**
     * Fetches the appropriate active notification template based on the given criteria
     * and renders it using the provided payload.
     *
     * This method would typically:
     * 1. Query the {@link com.wfm.experts.notificationengine.repository.NotificationTemplateRepository}
     * to find the latest active version of the template matching the templateId, channel, and language.
     * 2. Use a templating engine (e.g., Thymeleaf, FreeMarker, StringSubstitutor) to replace
     * placeholders in the template's subject and body with values from the payload.
     *
     * @param templateId The unique identifier of the template (e.g., "user_welcome").
     * @param channel    The channel for which the template is intended.
     * @param language   The desired language code for the template (e.g., "en-US").
     * The implementation might have a fallback to a default language if the specified one is not found.
     * @param payload    A map of key-value pairs containing the data to be injected into the template.
     * @return An {@link Optional} containing {@link RenderedTemplateContent} with the processed subject (if applicable)
     * and body if a suitable template is found and processed successfully.
     * Returns an empty Optional if no active template is found or if rendering fails.
     * @throws TemplateProcessingException if an error occurs during template rendering (e.g., syntax error in template).
     */
    Optional<RenderedTemplateContent> getAndRenderTemplate(
            String templateId,
            NotificationRequest.ChannelType channel,
            String language, // Could also be sourced from user preferences or a default
            Map<String, Object> payload
    ) throws TemplateProcessingException;

    /**
     * (Optional) Fetches a raw notification template without rendering.
     * This might be useful for admin UIs or for systems that do their own rendering.
     *
     * @param templateId The unique identifier of the template.
     * @param channel    The channel.
     * @param language   The language code.
     * @param version    The specific version of the template. If null, the latest active version might be fetched.
     * @return An {@link Optional} containing the {@link NotificationTemplate} entity.
     */
    Optional<NotificationTemplate> getRawTemplate(
            String templateId,
            NotificationRequest.ChannelType channel,
            String language,
            Integer version // Nullable to fetch latest active
    );

    /**
     * Custom exception for errors during template processing.
     */
    class TemplateProcessingException extends RuntimeException {
        public TemplateProcessingException(String message) {
            super(message);
        }

        public TemplateProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

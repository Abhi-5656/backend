package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.entity.NotificationTemplate;
import com.wfm.experts.notificationengine.repository.NotificationTemplateRepository;
import com.wfm.experts.notificationengine.service.TemplatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link TemplatingService}.
 * Handles fetching notification templates and rendering them with provided data.
 */
@Service
public class TemplatingServiceImpl implements TemplatingService {

    private static final Logger logger = LoggerFactory.getLogger(TemplatingServiceImpl.class);

    // Regex to find placeholders like {{key}} or {{ key.subkey }}
    // Allows for optional spaces around the key.
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*\\}\\}");


    private final NotificationTemplateRepository templateRepository;

    @Value("${notification.templating.default-language:en-US}") // Default language if not found
    private String defaultLanguage;

    @Autowired
    public TemplatingServiceImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional(readOnly = true) // Ensures LOB access is within a transaction
    public Optional<RenderedTemplateContent> getAndRenderTemplate(
            String templateId,
            NotificationRequest.ChannelType channel,
            String language,
            Map<String, Object> payload) throws TemplateProcessingException {

        if (templateId == null || channel == null || language == null) {
            logger.warn("TemplateId, channel, or language is null. Cannot fetch template.");
            return Optional.empty();
        }

        // Attempt to fetch the template for the specified language
        Optional<NotificationTemplate> templateOptional =
                templateRepository.findFirstByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
                        templateId, channel, language);

        // System.out.println(templateOptional+"Template Optional"); // Original debug line

        // If not found for the specified language, try with the default language as a fallback
        if (templateOptional.isEmpty() && !language.equals(defaultLanguage)) {
            logger.warn("Template ID '{}' for channel '{}' not found for language '{}'. Attempting fallback to default language '{}'.",
                    templateId, channel, language, defaultLanguage);
            templateOptional = templateRepository.findFirstByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
                    templateId, channel, defaultLanguage);
        }

        if (templateOptional.isEmpty()) {
            logger.error("No active template found for ID '{}', channel '{}', language '{}' (or default language '{}').",
                    templateId, channel, language, defaultLanguage);
            // No need to throw TemplateProcessingException here for not found,
            // Optional.empty() is the correct return for "not found".
            // The orElseThrow in the calling service (AppNotificationServiceImpl) handles the case where it *expects* a template.
            return Optional.empty();
        }

        NotificationTemplate template = templateOptional.get();
        logger.debug("Found template: ID='{}', Version='{}', Language='{}'",
                template.getTemplateId(), template.getVersion(), template.getLanguage());

        try {
            String renderedBody = render(template.getBody(), payload);
            String renderedSubject = null;
            if (template.getSubject() != null && !template.getSubject().trim().isEmpty()) {
                renderedSubject = render(template.getSubject(), payload);
            }
            return Optional.of(new RenderedTemplateContent(renderedSubject, renderedBody));
        } catch (Exception e) { // Catch broader exceptions during rendering itself if any
            logger.error("Error rendering template ID '{}', Version '{}'. Error: {}",
                    template.getTemplateId(), template.getVersion(), e.getMessage(), e);
            throw new TemplateProcessingException("Failed to render template " + template.getTemplateId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Ensures LOB access is within a transaction
    public Optional<NotificationTemplate> getRawTemplate(
            String templateId,
            NotificationRequest.ChannelType channel,
            String language,
            Integer version) { // If version is null, fetch latest active

        if (templateId == null || channel == null || language == null) {
            logger.warn("TemplateId, channel, or language is null. Cannot fetch raw template.");
            return Optional.empty();
        }

        if (version != null) {
            return templateRepository.findByTemplateIdAndChannelAndLanguageAndVersionAndIsActiveTrue(
                    templateId, channel, language, version);
        } else {
            // Fallback to latest active version if specific version not requested
            Optional<NotificationTemplate> templateOptional =
                    templateRepository.findFirstByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
                            templateId, channel, language);

            if (templateOptional.isEmpty() && !language.equals(defaultLanguage)) {
                logger.warn("Raw template ID '{}' for channel '{}' not found for language '{}'. Attempting fallback to default language '{}'.",
                        templateId, channel, language, defaultLanguage);
                return templateRepository.findFirstByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
                        templateId, channel, defaultLanguage);
            }
            return templateOptional;
        }
    }

    /**
     * Renders a template string by replacing placeholders with values from the payload.
     * Placeholders are in the format {{key}}.
     * If a key is not found in the payload, the placeholder might be left as is or replaced with an empty string,
     * depending on desired behavior (currently leaves as is if not found, or empty string if value is null).
     *
     * @param templateString The string containing placeholders.
     * @param payload        The map of data to replace placeholders.
     * @return The rendered string.
     */
    private String render(String templateString, Map<String, Object> payload) {
        if (templateString == null) {
            return null;
        }
        if (payload == null || payload.isEmpty()) {
            return templateString; // No data to replace placeholders
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateString);

        while (matcher.find()) {
            String key = matcher.group(1).trim(); // Get the key, trimmed of spaces
            Object value = getNestedValue(payload, key); // Support for basic nested keys like "user.name"

            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
            } else {
                // Behavior for missing keys:
                // Option 1: Leave placeholder as is
                // matcher.appendReplacement(sb, matcher.group(0));
                // Option 2: Replace with empty string (or a default like "[MISSING_KEY]")
                matcher.appendReplacement(sb, ""); // Current behavior: replace missing/null with empty string
                logger.warn("Placeholder key '{}' not found in payload or value is null for template rendering. Replaced with empty string.", key);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Retrieves a value from the payload, supporting basic dot-notation for nested objects.
     * Example: "user.address.city"
     *
     * @param payload The main payload map.
     * @param key     The key, which can be dot-separated for nesting.
     * @return The value if found, otherwise null.
     */
    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> payload, String key) {
        if (key.contains(".")) {
            String[] keys = key.split("\\.", 2); // Split only on the first dot
            Object nestedObject = payload.get(keys[0]);
            if (nestedObject instanceof Map) {
                // Recursively get the value from the nested map
                return getNestedValue((Map<String, Object>) nestedObject, keys[1]);
            } else {
                // If the path doesn't lead to another map, but there are still dots, the key is not found as nested.
                return null;
            }
        } else {
            // Base case: no dots, just get the value directly from the current map
            return payload.get(key);
        }
    }
}
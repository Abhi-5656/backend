package com.wfm.experts.notificationengine.service;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // Added this import
import com.wfm.experts.notificationengine.dto.NotificationTemplateDto;
import com.wfm.experts.notificationengine.exception.TemplateNotFoundException; // Custom exception
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing NotificationTemplates (CRUD operations).
 */
public interface NotificationTemplateService {

    /**
     * Creates a new notification template.
     *
     * @param templateDto DTO containing the details of the template to create.
     * @return The created NotificationTemplateDto with its generated ID and timestamps.
     * @throws com.wfm.experts.notificationengine.exception.DuplicateTemplateException if a template with the same templateId, channel, language, and version already exists.
     */
    NotificationTemplateDto createTemplate(NotificationTemplateDto templateDto);

    /**
     * Retrieves a notification template by its database ID.
     *
     * @param id The database ID of the template.
     * @return An Optional containing the NotificationTemplateDto if found, otherwise empty.
     */
    Optional<NotificationTemplateDto> getTemplateById(Long id);

    /**
     * Retrieves a specific version of a template by its logical identifiers.
     *
     * @param templateId The logical ID of the template (e.g., "user_welcome_email").
     * @param channel    The channel type.
     * @param language   The language code.
     * @param version    The version number.
     * @return An Optional containing the NotificationTemplateDto if found.
     */
    Optional<NotificationTemplateDto> getTemplateByLogicId(String templateId, NotificationRequest.ChannelType channel, String language, int version);


    /**
     * Retrieves all notification templates, with pagination.
     *
     * @param pageable Pagination information.
     * @return A Page of NotificationTemplateDto.
     */
    Page<NotificationTemplateDto> getAllTemplates(Pageable pageable);

    /**
     * Retrieves all versions of a specific template ID for a given channel and language.
     * @param templateId The logical ID of the template.
     * @param channel The channel.
     * @param language The language.
     * @return A list of NotificationTemplateDto.
     */
    List<NotificationTemplateDto> getAllVersionsOfTemplate(String templateId, NotificationRequest.ChannelType channel, String language);


    /**
     * Updates an existing notification template.
     *
     * @param id          The database ID of the template to update.
     * @param templateDto DTO containing the updated details.
     * @return The updated NotificationTemplateDto.
     * @throws TemplateNotFoundException if no template with the given ID is found.
     * @throws com.wfm.experts.notificationengine.exception.DuplicateTemplateException if the update would result in a duplicate template (based on templateId, channel, language, version).
     */
    NotificationTemplateDto updateTemplate(Long id, NotificationTemplateDto templateDto) throws TemplateNotFoundException;

    /**
     * Deletes a notification template by its database ID.
     *
     * @param id The database ID of the template to delete.
     * @throws TemplateNotFoundException if no template with the given ID is found.
     */
    void deleteTemplate(Long id) throws TemplateNotFoundException;
}

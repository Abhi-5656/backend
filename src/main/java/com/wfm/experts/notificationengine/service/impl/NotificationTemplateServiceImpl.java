package com.wfm.experts.notificationengine.service.impl;

import com.wfm.experts.notificationengine.dto.NotificationRequest; // For ChannelType
import com.wfm.experts.notificationengine.dto.NotificationTemplateDto;
import com.wfm.experts.notificationengine.entity.NotificationTemplate;
import com.wfm.experts.notificationengine.exception.DuplicateTemplateException;
import com.wfm.experts.notificationengine.exception.TemplateNotFoundException;
import com.wfm.experts.notificationengine.repository.NotificationTemplateRepository;
import com.wfm.experts.notificationengine.service.NotificationTemplateService; // Corrected interface name
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.dao.DataIntegrityViolationException; // Not directly throwing this, but repository might
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NotificationTemplateService}.
 * Handles CRUD operations for NotificationTemplates.
 */
@Service
@Transactional // Apply transactionality to all public methods by default
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplateServiceImpl.class);

    private final NotificationTemplateRepository templateRepository;

    @Autowired
    public NotificationTemplateServiceImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public NotificationTemplateDto createTemplate(NotificationTemplateDto templateDto) {
        logger.info("Attempting to create template with templateId: '{}', channel: {}, language: '{}', version: {}",
                templateDto.getTemplateId(), templateDto.getChannel(), templateDto.getLanguage(), templateDto.getVersion());

        // Check for duplicates based on the unique constraint (templateId, channel, language, version)
        // The unique constraint in the DB table is on (template_id, language, version, channel)
        if (templateRepository.existsByTemplateIdAndChannelAndLanguageAndVersion(
                templateDto.getTemplateId(),
                templateDto.getChannel(),
                templateDto.getLanguage(),
                templateDto.getVersion() == null ? 1 : templateDto.getVersion() // Ensure version is checked, default to 1 if DTO's is null
        )) {
            String errorMsg = String.format("A template with ID '%s', channel '%s', language '%s', and version '%d' already exists.",
                    templateDto.getTemplateId(), templateDto.getChannel(), templateDto.getLanguage(),
                    templateDto.getVersion() == null ? 1 : templateDto.getVersion());
            logger.warn(errorMsg);
            throw new DuplicateTemplateException(errorMsg);
        }

        NotificationTemplate templateEntity = convertToEntity(templateDto);
        // ID, createdAt, updatedAt are handled by JPA @PrePersist/@PreUpdate or DB defaults
        NotificationTemplate savedEntity = templateRepository.save(templateEntity);
        logger.info("Successfully created template with DB ID: {}", savedEntity.getId());
        return convertToDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplateDto> getTemplateById(Long id) {
        logger.debug("Fetching template by DB ID: {}", id);
        return templateRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplateDto> getTemplateByLogicId(String templateId, NotificationRequest.ChannelType channel, String language, int version) {
        logger.debug("Fetching template by logicId: '{}', channel: {}, language: '{}', version: {}",
                templateId, channel, language, version);
        // This query already filters by isActiveTrue in the repository method
        return templateRepository.findByTemplateIdAndChannelAndLanguageAndVersionAndIsActiveTrue(
                        templateId, channel, language, version)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplateDto> getAllTemplates(Pageable pageable) {
        logger.debug("Fetching all templates with pagination: {}", pageable);
        return templateRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplateDto> getAllVersionsOfTemplate(String templateId, NotificationRequest.ChannelType channel, String language) {
        logger.debug("Fetching all active versions for templateId: '{}', channel: {}, language: '{}'", templateId, channel, language);
        return templateRepository.findByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(templateId, channel, language)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    public NotificationTemplateDto updateTemplate(Long id, NotificationTemplateDto templateDto) throws TemplateNotFoundException {
        logger.info("Attempting to update template with DB ID: {}", id);
        NotificationTemplate existingEntity = templateRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update failed. Template not found with DB ID: {}", id);
                    return new TemplateNotFoundException("NotificationTemplate not found with id: " + id);
                });

        // Check if the update would cause a duplicate violation for the logical key,
        // excluding the current entity itself.
        // The unique constraint is on (template_id, language, version, channel)
        Optional<NotificationTemplate> conflictingEntityOpt = templateRepository
                .findByTemplateIdAndChannelAndLanguageAndVersionAndIsActiveTrue( // Consider if isActiveTrue should be part of this check
                        templateDto.getTemplateId(),
                        templateDto.getChannel(),
                        templateDto.getLanguage(),
                        templateDto.getVersion() == null ? existingEntity.getVersion() : templateDto.getVersion()
                );

        if (conflictingEntityOpt.isPresent() && !conflictingEntityOpt.get().getId().equals(id)) {
            String errorMsg = String.format("An update would result in a duplicate template with ID '%s', channel '%s', language '%s', and version '%d'. Another template (DB ID: %d) already has this combination.",
                    templateDto.getTemplateId(), templateDto.getChannel(), templateDto.getLanguage(),
                    templateDto.getVersion() == null ? existingEntity.getVersion() : templateDto.getVersion(),
                    conflictingEntityOpt.get().getId());
            logger.warn(errorMsg);
            throw new DuplicateTemplateException(errorMsg);
        }

        // Update fields from DTO (excluding ID, createdAt)
        // updatedAt will be handled by @PreUpdate
        existingEntity.setTemplateId(templateDto.getTemplateId());
        existingEntity.setChannel(templateDto.getChannel());
        existingEntity.setLanguage(templateDto.getLanguage());
        if (templateDto.getVersion() != null) { // Only update version if provided in DTO
            existingEntity.setVersion(templateDto.getVersion());
        }
        existingEntity.setSubject(templateDto.getSubject());
        existingEntity.setBody(templateDto.getBody());
        existingEntity.setActive(templateDto.isActive());
        existingEntity.setDescription(templateDto.getDescription());
        // existingEntity.setUpdatedAt(LocalDateTime.now()); // Done by @PreUpdate

        NotificationTemplate updatedEntity = templateRepository.save(existingEntity);
        logger.info("Successfully updated template with DB ID: {}", updatedEntity.getId());
        return convertToDto(updatedEntity);
    }

    @Override
    public void deleteTemplate(Long id) throws TemplateNotFoundException {
        logger.info("Attempting to delete template with DB ID: {}", id);
        if (!templateRepository.existsById(id)) {
            logger.warn("Delete failed. Template not found with DB ID: {}", id);
            throw new TemplateNotFoundException("NotificationTemplate not found with id: " + id + " for deletion.");
        }
        templateRepository.deleteById(id);
        logger.info("Successfully deleted template with DB ID: {}", id);
    }

    // Helper methods for conversion between Entity and DTO
    private NotificationTemplateDto convertToDto(NotificationTemplate entity) {
        if (entity == null) {
            return null;
        }
        NotificationTemplateDto dto = new NotificationTemplateDto();
        BeanUtils.copyProperties(entity, dto);
        // Ensure ChannelType enum is correctly copied if BeanUtils doesn't handle it directly
        // (it should if names match and type is compatible or a converter is registered)
        dto.setChannel(entity.getChannel()); // Explicitly set if needed
        return dto;
    }

    private NotificationTemplate convertToEntity(NotificationTemplateDto dto) {
        if (dto == null) {
            return null;
        }
        NotificationTemplate entity = new NotificationTemplate();
        // Exclude 'id', 'createdAt', 'updatedAt' when copying from DTO to a new entity,
        // as these are typically managed by the database or JPA lifecycle.
        // For updates, 'id' is used to fetch the entity, and 'createdAt' is not changed.
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt");
        entity.setChannel(dto.getChannel()); // Explicitly set if needed
        return entity;
    }
}

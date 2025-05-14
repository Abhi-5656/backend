package com.wfm.experts.notificationengine.controller;

import com.wfm.experts.notificationengine.dto.NotificationRequest;
import com.wfm.experts.notificationengine.dto.NotificationTemplateDto;
import com.wfm.experts.notificationengine.exception.TemplateNotFoundException;
import com.wfm.experts.notificationengine.service.NotificationTemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification-templates") // Removed /v1
public class NotificationTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplateController.class);

    private final NotificationTemplateService templateService;

    @Autowired
    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateDto> createTemplate(@Valid @RequestBody NotificationTemplateDto templateDto) {
        logger.info("Received request to create notification template with templateId: {}", templateDto.getTemplateId());
        NotificationTemplateDto createdTemplate = templateService.createTemplate(templateDto);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDto> getTemplateById(@PathVariable Long id) {
        logger.info("Received request to get template by DB ID: {}", id);
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find")
    public ResponseEntity<NotificationTemplateDto> getTemplateByLogicId(
            @RequestParam String templateId,
            @RequestParam NotificationRequest.ChannelType channel,
            @RequestParam String language,
            @RequestParam int version) {
        logger.info("Received request to find template by logicId: {}, channel: {}, language: {}, version: {}",
                templateId, channel, language, version);
        return templateService.getTemplateByLogicId(templateId, channel, language, version)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/versions")
    public ResponseEntity<List<NotificationTemplateDto>> getAllVersionsOfTemplate(
            @RequestParam String templateId,
            @RequestParam NotificationRequest.ChannelType channel,
            @RequestParam String language) {
        logger.info("Received request to get all versions for templateId: {}, channel: {}, language: {}",
                templateId, channel, language);
        List<NotificationTemplateDto> templates = templateService.getAllVersionsOfTemplate(templateId, channel, language);
        if (templates.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(templates);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationTemplateDto>> getAllTemplates(@PageableDefault(size = 20) Pageable pageable) {
        logger.info("Received request to get all templates with pagination: {}", pageable);
        Page<NotificationTemplateDto> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(@PathVariable Long id, @Valid @RequestBody NotificationTemplateDto templateDto) {
        logger.info("Received request to update template with DB ID: {}", id);
        try {
            NotificationTemplateDto updatedTemplate = templateService.updateTemplate(id, templateDto);
            return ResponseEntity.ok(updatedTemplate);
        } catch (TemplateNotFoundException e) {
            logger.warn("Update failed. Template not found with DB ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        logger.info("Received request to delete template with DB ID: {}", id);
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (TemplateNotFoundException e) {
            logger.warn("Delete failed. Template not found with DB ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}

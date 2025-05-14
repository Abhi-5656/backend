package com.wfm.experts.notificationengine.repository;

import com.wfm.experts.notificationengine.entity.NotificationTemplate;
import com.wfm.experts.notificationengine.dto.NotificationRequest.ChannelType; // Assuming ChannelType is in NotificationRequest
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link NotificationTemplate} entity.
 * This repository will operate within the context of the current tenant's schema.
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Finds the latest active version of a template by its templateId, channel, and language.
     * It orders by version in descending order and takes the first result.
     *
     * @param templateId The unique identifier of the template (e.g., "user_welcome").
     * @param channel    The channel for which the template is designed (e.g., EMAIL, SMS).
     * @param language   The language code of the template (e.g., "en-US").
     * @return An {@link Optional} containing the latest active {@link NotificationTemplate} if found.
     */
    Optional<NotificationTemplate> findFirstByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
            String templateId, ChannelType channel, String language);

    /**
     * Finds all active versions of a template for a specific templateId, channel, and language,
     * ordered by version descending.
     *
     * @param templateId The unique identifier of the template.
     * @param channel    The notification channel.
     * @param language   The language code.
     * @return A list of active {@link NotificationTemplate}s.
     */
    List<NotificationTemplate> findByTemplateIdAndChannelAndLanguageAndIsActiveTrueOrderByVersionDesc(
            String templateId, ChannelType channel, String language);

    /**
     * Finds a specific active version of a template.
     *
     * @param templateId The unique identifier of the template.
     * @param channel    The notification channel.
     * @param language   The language code.
     * @param version    The specific version number.
     * @return An {@link Optional} containing the specific active {@link NotificationTemplate} if found.
     */
    Optional<NotificationTemplate> findByTemplateIdAndChannelAndLanguageAndVersionAndIsActiveTrue(
            String templateId, ChannelType channel, String language, int version);

    /**
     * Finds all templates (active and inactive, all versions) by templateId.
     * Useful for administrative purposes to see all variations of a template.
     *
     * @param templateId The unique identifier of the template.
     * @return A list of all {@link NotificationTemplate}s matching the templateId.
     */
    List<NotificationTemplate> findByTemplateId(String templateId);

    /**
     * Finds all active templates for a given channel and language.
     * This could be used to list available templates for a specific context.
     *
     * @param channel  The notification channel.
     * @param language The language code.
     * @return A list of active {@link NotificationTemplate}s.
     */
    List<NotificationTemplate> findByChannelAndLanguageAndIsActiveTrue(ChannelType channel, String language);

    /**
     * Custom query to check if a template with the given ID, channel, language, and version exists.
     * This can be useful before attempting to create a new template to avoid constraint violations.
     *
     * @param templateId The template ID.
     * @param channel    The channel type.
     * @param language   The language code.
     * @param version    The version number.
     * @return true if such a template exists, false otherwise.
     */
    @Query("SELECT CASE WHEN COUNT(nt) > 0 THEN true ELSE false END " +
            "FROM NotificationTemplate nt " +
            "WHERE nt.templateId = :templateId " +
            "AND nt.channel = :channel " +
            "AND nt.language = :language " +
            "AND nt.version = :version")
    boolean existsByTemplateIdAndChannelAndLanguageAndVersion(
            @Param("templateId") String templateId,
            @Param("channel") ChannelType channel,
            @Param("language") String language,
            @Param("version") int version
    );
}

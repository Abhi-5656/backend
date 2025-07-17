package com.wfm.experts.tenant.common.setup.wizard.mapper;

import com.wfm.experts.tenant.common.setup.wizard.dto.SetupWizardDto;
import com.wfm.experts.tenant.common.setup.wizard.entity.SetupWizard;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for the entity {@link SetupWizard} and its DTO {@link SetupWizardDto}.
 * This mapper uses MapStruct to automate the conversion between the two objects.
 */
@Mapper(componentModel = "spring")
public interface SetupWizardMapper {

    /**
     * An instance of the mapper that can be used for manual conversions if needed.
     */
    SetupWizardMapper INSTANCE = Mappers.getMapper(SetupWizardMapper.class);

    /**
     * Converts a SetupWizard entity to a SetupWizardDto.
     *
     * @param entity The SetupWizard entity to convert.
     * @return The corresponding SetupWizardDto.
     */
    SetupWizardDto toDto(SetupWizard entity);

    /**
     * Converts a SetupWizardDto to a SetupWizard entity.
     *
     * @param dto The SetupWizardDto to convert.
     * @return The corresponding SetupWizard entity.
     */
    SetupWizard toEntity(SetupWizardDto dto);
}
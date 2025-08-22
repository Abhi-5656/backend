package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Main mapper for converting between LeavePolicy entities and DTOs.
 * It uses other specialized mappers for nested configuration objects.
 */
@Mapper(
        componentModel = "spring",
        uses = {
                CalculationDateConfigMapper.class,
                GrantsConfigMapper.class,
                LimitsConfigMapper.class,
                AttachmentsConfigMapper.class
        }
)
public interface LeavePolicyMapper {

    /**
     * Converts a LeavePolicy entity to a LeavePolicyDto.
     * @param leavePolicy The entity to convert.
     * @return The corresponding DTO.
     */
    LeavePolicyDto toDto(LeavePolicy leavePolicy);

    /**
     * Converts a LeavePolicyDto to a LeavePolicy entity.
     * @param leavePolicyDto The DTO to convert.
     * @return The corresponding entity.
     */
    LeavePolicy toEntity(LeavePolicyDto leavePolicyDto);

    /**
     * Updates an existing LeavePolicy entity from a DTO.
     * This is useful for PATCH/PUT operations to avoid creating new objects.
     * Null values in the DTO will be ignored during the update.
     * @param dto The source DTO with new values.
     * @param entity The target entity to be updated.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLeavePolicyFromDto(LeavePolicyDto dto, @MappingTarget LeavePolicy entity);
}
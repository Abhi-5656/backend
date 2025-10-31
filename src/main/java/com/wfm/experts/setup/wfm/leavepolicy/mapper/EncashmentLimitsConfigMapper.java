package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EncashmentLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EncashmentLimitsConfig;
import org.mapstruct.BeanMapping; // <-- ADD THIS
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget; // <-- ADD THIS
import org.mapstruct.NullValuePropertyMappingStrategy; // <-- ADD THIS

@Mapper(componentModel = "spring")
public interface EncashmentLimitsConfigMapper {
    EncashmentLimitsConfigDto toDto(EncashmentLimitsConfig entity);
    EncashmentLimitsConfig toEntity(EncashmentLimitsConfigDto dto);

    // --- ADD THIS METHOD ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EncashmentLimitsConfigDto dto, @MappingTarget EncashmentLimitsConfig entity);
    // --- END OF FIX ---
}
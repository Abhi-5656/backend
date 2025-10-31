package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.CarryForwardLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.CarryForwardLimitsConfig;
import org.mapstruct.BeanMapping; // <-- ADD THIS
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget; // <-- ADD THIS
import org.mapstruct.NullValuePropertyMappingStrategy; // <-- ADD THIS

@Mapper(componentModel = "spring")
public interface CarryForwardLimitsConfigMapper {
    CarryForwardLimitsConfigDto toDto(CarryForwardLimitsConfig entity);
    CarryForwardLimitsConfig toEntity(CarryForwardLimitsConfigDto dto);

    // --- ADD THIS METHOD ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CarryForwardLimitsConfigDto dto, @MappingTarget CarryForwardLimitsConfig entity);
    // --- END OF FIX ---
}
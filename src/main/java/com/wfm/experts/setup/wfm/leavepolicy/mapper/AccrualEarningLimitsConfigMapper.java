package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.AccrualEarningLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.AccrualEarningLimitsConfig;
import org.mapstruct.BeanMapping; // <-- ADD THIS
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget; // <-- ADD THIS
import org.mapstruct.NullValuePropertyMappingStrategy; // <-- ADD THIS

@Mapper(componentModel = "spring")
public interface AccrualEarningLimitsConfigMapper {
    AccrualEarningLimitsConfigDto toDto(AccrualEarningLimitsConfig entity);
    AccrualEarningLimitsConfig toEntity(AccrualEarningLimitsConfigDto dto);

    // --- ADD THIS METHOD ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccrualEarningLimitsConfigDto dto, @MappingTarget AccrualEarningLimitsConfig entity);
    // --- END OF FIX ---
}
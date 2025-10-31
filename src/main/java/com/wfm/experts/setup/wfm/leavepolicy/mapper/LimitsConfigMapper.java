package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LimitsConfig;
import org.mapstruct.BeanMapping; // <-- ADD THIS
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget; // <-- ADD THIS
import org.mapstruct.NullValuePropertyMappingStrategy; // <-- ADD THIS

@Mapper(
        componentModel = "spring",
        uses = {
                AccrualEarningLimitsConfigMapper.class,
                CarryForwardLimitsConfigMapper.class,
                EncashmentLimitsConfigMapper.class
        }
)
public interface LimitsConfigMapper {
    LimitsConfigDto toDto(LimitsConfig entity);
    LimitsConfig toEntity(LimitsConfigDto dto);

    // --- ADD THIS METHOD ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(LimitsConfigDto dto, @MappingTarget LimitsConfig entity);
    // --- END OF FIX ---
}
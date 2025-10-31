package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.CarryForwardLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.CarryForwardLimitsConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarryForwardLimitsConfigMapper {
    CarryForwardLimitsConfigDto toDto(CarryForwardLimitsConfig entity);
    CarryForwardLimitsConfig toEntity(CarryForwardLimitsConfigDto dto);
}
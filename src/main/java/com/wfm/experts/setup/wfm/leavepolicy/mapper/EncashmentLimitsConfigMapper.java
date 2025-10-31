package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EncashmentLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EncashmentLimitsConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EncashmentLimitsConfigMapper {
    EncashmentLimitsConfigDto toDto(EncashmentLimitsConfig entity);
    EncashmentLimitsConfig toEntity(EncashmentLimitsConfigDto dto);
}
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EncashmentLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EncashmentConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EncashmentConfigMapper {
    EncashmentLimitsConfigDto toDto(EncashmentConfig entity);
    EncashmentConfig toEntity(EncashmentLimitsConfigDto dto);
}
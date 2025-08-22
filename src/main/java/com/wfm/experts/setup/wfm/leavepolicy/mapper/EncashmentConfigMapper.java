package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EncashmentConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EncashmentConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EncashmentConfigMapper {
    EncashmentConfigDto toDto(EncashmentConfig entity);
    EncashmentConfig toEntity(EncashmentConfigDto dto);
}
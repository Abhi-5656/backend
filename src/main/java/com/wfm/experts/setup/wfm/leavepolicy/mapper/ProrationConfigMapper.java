package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ProrationConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ProrationConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProrationConfigMapper {
    ProrationConfigDto toDto(ProrationConfig entity);
    ProrationConfig toEntity(ProrationConfigDto dto);
}
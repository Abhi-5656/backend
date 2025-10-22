package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ProrationConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ProrationConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // <-- IMPORT THIS

@Mapper(componentModel = "spring")
public interface ProrationConfigMapper {

    // Add this explicit mapping
    @Mapping(target = "isEnabled", source = "enabled") // MapStruct understands "enabled" from "isEnabled()"
    ProrationConfigDto toDto(ProrationConfig entity);

    ProrationConfig toEntity(ProrationConfigDto dto);
}
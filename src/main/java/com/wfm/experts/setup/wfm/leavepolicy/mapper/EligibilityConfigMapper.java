package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EligibilityConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EligibilityConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EligibilityConfigMapper {
    EligibilityConfigDto toDto(EligibilityConfig entity);
    EligibilityConfig toEntity(EligibilityConfigDto dto);
}
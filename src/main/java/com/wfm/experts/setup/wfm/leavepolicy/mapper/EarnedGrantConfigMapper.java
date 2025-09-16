package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.EarnedGrantConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EarnedGrantConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProrationConfigMapper.class})
public interface EarnedGrantConfigMapper {
    EarnedGrantConfigDto toDto(EarnedGrantConfig entity);
    EarnedGrantConfig toEntity(EarnedGrantConfigDto dto);
}
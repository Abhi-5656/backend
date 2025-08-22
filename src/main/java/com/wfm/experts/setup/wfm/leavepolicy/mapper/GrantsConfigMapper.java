package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.GrantsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                FixedGrantConfigMapper.class,
                EarnedGrantConfigMapper.class
        }
)
public interface GrantsConfigMapper {
    GrantsConfigDto toDto(GrantsConfig entity);
    GrantsConfig toEntity(GrantsConfigDto dto);
}
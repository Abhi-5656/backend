package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LimitsConfig;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                CarryForwardConfigMapper.class,
                EncashmentConfigMapper.class,
                EligibilityConfigMapper.class
        }
)
public interface LimitsConfigMapper {
    LimitsConfigDto toDto(LimitsConfig entity);
    LimitsConfig toEntity(LimitsConfigDto dto);
}
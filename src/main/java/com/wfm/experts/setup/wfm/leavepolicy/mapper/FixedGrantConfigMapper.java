package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.FixedGrantConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                // Add these mappers so MapStruct knows how to map the nested objects
                OneTimeGrantDetailsMapper.class,
                RepeatedlyGrantDetailsMapper.class
        }
)
public interface FixedGrantConfigMapper {

    // Add the mapping methods
    FixedGrantConfigDto toDto(FixedGrantConfig entity);

    FixedGrantConfig toEntity(FixedGrantConfigDto dto);
}
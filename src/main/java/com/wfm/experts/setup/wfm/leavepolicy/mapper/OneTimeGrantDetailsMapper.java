package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.OneTimeGrantDetailsDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.OneTimeGrantDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OneTimeGrantDetailsMapper {
    OneTimeGrantDetailsDto toDto(OneTimeGrantDetails entity);
    OneTimeGrantDetails toEntity(OneTimeGrantDetailsDto dto);
}
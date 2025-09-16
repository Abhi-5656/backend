package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.RepeatedlyGrantDetailsDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.RepeatedlyGrantDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProrationConfigMapper.class})
public interface RepeatedlyGrantDetailsMapper {
    RepeatedlyGrantDetailsDto toDto(RepeatedlyGrantDetails entity);
    RepeatedlyGrantDetails toEntity(RepeatedlyGrantDetailsDto dto);
}
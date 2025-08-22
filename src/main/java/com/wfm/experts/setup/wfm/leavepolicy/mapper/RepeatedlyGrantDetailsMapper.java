package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.RepeatedlyGrantDetailsDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.RepeatedlyGrantDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RepeatedlyGrantDetailsMapper {
    RepeatedlyGrantDetailsDto toDto(RepeatedlyGrantDetails entity);
    RepeatedlyGrantDetails toEntity(RepeatedlyGrantDetailsDto dto);
}
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.AttachmentsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.AttachmentsConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttachmentsConfigMapper {
    AttachmentsConfigDto toDto(AttachmentsConfig entity);
    AttachmentsConfig toEntity(AttachmentsConfigDto dto);
}
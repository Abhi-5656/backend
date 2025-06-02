package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.dto.AttendanceRuleDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.AttendanceRule;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceRuleMapper {

    AttendanceRuleMapper INSTANCE = Mappers.getMapper(AttendanceRuleMapper.class);

    AttendanceRuleDTO toDto(AttendanceRule entity);

    AttendanceRule toEntity(AttendanceRuleDTO dto);
}

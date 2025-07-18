package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.dto.WeekendPayRulesDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.WeekendPayRules;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeekendPayRulesMapper {
    WeekendPayRulesDTO toDto(WeekendPayRules entity);
    WeekendPayRules toEntity(WeekendPayRulesDTO dto);
}
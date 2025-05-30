package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.HolidayPayRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.HolidayPayRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HolidayPayRulesMapper {
    HolidayPayRulesDTO toDto(HolidayPayRules entity);
    HolidayPayRules toEntity(HolidayPayRulesDTO dto);
}

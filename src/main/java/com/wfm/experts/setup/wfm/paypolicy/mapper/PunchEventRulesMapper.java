package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.PunchEventRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.PunchEventRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PunchEventRulesMapper {
    PunchEventRulesDTO toDto(PunchEventRules entity);
    PunchEventRules toEntity(PunchEventRulesDTO dto);
}

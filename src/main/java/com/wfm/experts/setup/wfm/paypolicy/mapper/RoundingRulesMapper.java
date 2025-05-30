package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.RoundingRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.RoundingRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = RoundingRuleMapper.class)
public interface RoundingRulesMapper {
    RoundingRulesDTO toDto(RoundingRules entity);
    RoundingRules toEntity(RoundingRulesDTO dto);
}

package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.RoundingRule;
import com.wfm.experts.setup.wfm.paypolicy.dto.RoundingRuleDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoundingRuleMapper {
    RoundingRuleDTO toDto(RoundingRule entity);
    RoundingRule toEntity(RoundingRuleDTO dto);
}

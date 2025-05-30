package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.BreakRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.BreakRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BreakMapper.class)
public interface BreakRulesMapper {
    BreakRulesDTO toDto(BreakRules entity);
    BreakRules toEntity(BreakRulesDTO dto);
}

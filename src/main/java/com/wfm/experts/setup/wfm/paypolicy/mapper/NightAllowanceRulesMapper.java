package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.dto.NightAllowanceRulesDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.NightAllowanceRules;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NightAllowanceRulesMapper {
    NightAllowanceRulesDTO toDto(NightAllowanceRules entity);
    NightAllowanceRules toEntity(NightAllowanceRulesDTO dto);
}
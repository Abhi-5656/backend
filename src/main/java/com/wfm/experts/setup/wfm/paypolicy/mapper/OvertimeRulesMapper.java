package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.OvertimeRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.OvertimeRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PreShiftInclusionMapper.class)
public interface OvertimeRulesMapper {
    OvertimeRulesDTO toDto(OvertimeRules entity);
    OvertimeRules toEntity(OvertimeRulesDTO dto);
}

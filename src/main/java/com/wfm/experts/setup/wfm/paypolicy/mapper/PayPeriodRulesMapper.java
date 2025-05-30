package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.PayPeriodRules;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPeriodRulesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayPeriodRulesMapper {
    PayPeriodRulesDTO toDto(PayPeriodRules entity);
    PayPeriodRules toEntity(PayPeriodRulesDTO dto);
}

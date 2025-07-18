package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        RoundingRulesMapper.class,
        PunchEventRulesMapper.class,
        BreakRulesMapper.class,
        OvertimeRulesMapper.class,
        NightAllowanceRulesMapper.class,
        PayPeriodRulesMapper.class,
        HolidayPayRulesMapper.class,
        WeekendPayRulesMapper.class, // <-- ADDED
        ShiftMapper.class
})
public interface PayPolicyMapper {
    PayPolicyDTO toDto(PayPolicy entity);
    PayPolicy toEntity(PayPolicyDTO dto);
}
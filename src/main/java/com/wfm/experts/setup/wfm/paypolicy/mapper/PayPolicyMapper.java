package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        RoundingRulesMapper.class,
        PunchEventRulesMapper.class,
        BreakRulesMapper.class,
        OvertimeRulesMapper.class,
        NightAllowanceRulesMapper.class,
        PayPeriodRulesMapper.class,
        HolidayPayRulesMapper.class,
        WeekendPayRulesMapper.class, // <-- ADDED
        AttendanceRuleMapper.class, // <-- Ensures the consolidated rule is mapped
        ShiftMapper.class
})
public interface PayPolicyMapper {
    PayPolicyDTO toDto(PayPolicy entity);
    @Mapping(target = "roundingRules", expression = "java(dto.getRoundingRules() != null && dto.getRoundingRules().isEnabled() ? roundingRulesMapper.toEntity(dto.getRoundingRules()) : null)")
    @Mapping(target = "punchEventRules", expression = "java(dto.getPunchEventRules() != null && dto.getPunchEventRules().isEnabled() ? punchEventRulesMapper.toEntity(dto.getPunchEventRules()) : null)")
    @Mapping(target = "breakRules", expression = "java(dto.getBreakRules() != null && dto.getBreakRules().isEnabled() ? breakRulesMapper.toEntity(dto.getBreakRules()) : null)")
    @Mapping(target = "overtimeRules", expression = "java(dto.getOvertimeRules() != null && dto.getOvertimeRules().isEnabled() ? overtimeRulesMapper.toEntity(dto.getOvertimeRules()) : null)")
    @Mapping(target = "nightAllowanceRules", expression = "java(dto.getNightAllowanceRules() != null && dto.getNightAllowanceRules().isEnabled() ? nightAllowanceRulesMapper.toEntity(dto.getNightAllowanceRules()) : null)")
    @Mapping(target = "payPeriodRules", expression = "java(dto.getPayPeriodRules() != null && dto.getPayPeriodRules().isEnabled() ? payPeriodRulesMapper.toEntity(dto.getPayPeriodRules()) : null)")
    @Mapping(target = "holidayPayRules", expression = "java(dto.getHolidayPayRules() != null && dto.getHolidayPayRules().isEnabled() ? holidayPayRulesMapper.toEntity(dto.getHolidayPayRules()) : null)")
    @Mapping(target = "weekendPayRules", expression = "java(dto.getWeekendPayRules() != null && dto.getWeekendPayRules().isEnabled() ? weekendPayRulesMapper.toEntity(dto.getWeekendPayRules()) : null)")
    @Mapping(target = "attendanceRule", expression = "java(dto.getAttendanceRule() != null && dto.getAttendanceRule().getEnabledModes() != null && !dto.getAttendanceRule().getEnabledModes().isEmpty() ? attendanceRuleMapper.toEntity(dto.getAttendanceRule()) : null)")
    PayPolicy toEntity(PayPolicyDTO dto);
}
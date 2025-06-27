package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyDTO {
    private Long id;
    private String policyName;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private Boolean useFiloCalculation;

    private RoundingRulesDTO roundingRules;
    private PunchEventRulesDTO punchEventRules;
    private BreakRulesDTO breakRules;
    private OvertimeRulesDTO overtimeRules;
    private NightAllowanceRulesDTO nightAllowanceRules;
    private PayPeriodRulesDTO payPeriodRules;
    private HolidayPayRulesDTO holidayPayRules;
    private AttendanceRuleDTO attendanceRule;
}
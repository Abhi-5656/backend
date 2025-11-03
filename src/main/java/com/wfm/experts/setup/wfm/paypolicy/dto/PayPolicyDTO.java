package com.wfm.experts.setup.wfm.paypolicy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyDTO {
    private Long id;

    @NotBlank(message = "Policy name cannot be blank")
    private String policyName;

    @NotNull(message = "Effective date cannot be null")
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
    private WeekendPayRulesDTO weekendPayRules;
}
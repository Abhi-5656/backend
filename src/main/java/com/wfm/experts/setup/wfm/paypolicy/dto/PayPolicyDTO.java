package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyDTO {
    private Long id;
    private String policyName;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;

    private RoundingRulesDTO roundingRules;
    private PunchEventRulesDTO punchEventRules;
    private BreakRulesDTO breakRules;
    private OvertimeRulesDTO overtimeRules;
    private PayPeriodRulesDTO payPeriodRules;
    private HolidayPayRulesDTO holidayPayRules;
    private List<ShiftDTO> shifts;
}

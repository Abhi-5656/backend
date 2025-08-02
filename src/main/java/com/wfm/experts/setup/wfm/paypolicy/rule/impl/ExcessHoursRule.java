package com.wfm.experts.setup.wfm.paypolicy.rule.impl;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.entity.OvertimeRules;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;

public class ExcessHoursRule implements PayPolicyRule {

    @Override
    public String getName() {
        return "ExcessHoursRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // This rule should always run to capture any remaining time.
        return true;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        OvertimeRules overtimeSettings = context.getPayPolicy().getOvertimeRules();
        int excessMinutes = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
        int uncategorizedMinutes = (int) context.getFacts().getOrDefault("workedMinutes", 0);

        // 1. Claim any uncategorized minutes as excess time
        if (uncategorizedMinutes > 0) {
            excessMinutes += uncategorizedMinutes;
            context.getFacts().put("workedMinutes", 0); // Consume the minutes
        }

        // 2. Apply caps to OT, moving any spillover to excess
        if (overtimeSettings != null) {
            // Check Daily OT Cap
            if (overtimeSettings.getMaxOtPerDay() != null && overtimeSettings.getMaxOtPerDay() > 0) {
                int dailyOt = (int) context.getFacts().getOrDefault("dailyOtHoursMinutes", 0);
                int maxDailyOt = (int) (overtimeSettings.getMaxOtPerDay() * 60);
                if (dailyOt > maxDailyOt) {
                    excessMinutes += dailyOt - maxDailyOt;
                    context.getFacts().put("dailyOtHoursMinutes", maxDailyOt); // Cap the daily OT
                }
            }
            // A similar check for weekly OT cap would go here if needed
        }

        context.getFacts().put("excessHoursMinutes", excessMinutes);

        String message = String.format("Calculated %d minutes of excess hours.", excessMinutes);
        return buildResult("EXCESS_HOURS_CALCULATED", true, message);
    }

    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(result)
                .success(success)
                .message(message)
                .build();
    }
}
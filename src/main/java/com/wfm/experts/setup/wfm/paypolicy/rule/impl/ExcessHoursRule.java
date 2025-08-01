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
        // This rule only needs to run if overtime has been calculated.
        Integer dailyOt = (Integer) context.getFact("dailyOtHoursMinutes");
        Integer weeklyOt = (Integer) context.getFact("weeklyOtHoursMinutes");
        return (dailyOt != null && dailyOt > 0) || (weeklyOt != null && weeklyOt > 0);
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        OvertimeRules overtimeSettings = context.getPayPolicy().getOvertimeRules();
        if (overtimeSettings == null) {
            return buildResult("NO_OT_CONFIG", true, "Overtime not configured; cannot check for excess hours.");
        }

        int excessMinutes = 0;

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

        context.getFacts().put("excessHoursMinutes", excessMinutes);

        String message = String.format("Calculated %d minutes of excess hours after applying OT caps.", excessMinutes);
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
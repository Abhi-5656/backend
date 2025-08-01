package com.wfm.experts.setup.wfm.paypolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.entity.OvertimeRules;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import java.time.Duration;

public class StandardHoursRule implements PayPolicyRule {

    @Override
    public String getName() {
        return "StandardHoursRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // *** FIX: This rule should only run on standard workdays. ***
        boolean isHoliday = (boolean) context.getFacts().getOrDefault("isHoliday", false);
        boolean isWeekend = (boolean) context.getFacts().getOrDefault("isWeekend", false);

        Integer workedMinutes = (Integer) context.getFact("workedMinutes");

        return !isHoliday && !isWeekend && workedMinutes != null && workedMinutes > 0;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        OvertimeRules overtimeSettings = context.getPayPolicy().getOvertimeRules();
        if (overtimeSettings == null || !overtimeSettings.isEnableDailyOt()) {
            // If OT is not enabled, all remaining time is regular time.
            context.getFacts().put("regularMinutes", context.getFact("workedMinutes"));
            context.getFacts().put("workedMinutes", 0); // All minutes have been categorized.
            return buildResult("NO_OT_CONFIG", true, "Overtime not configured; all remaining time is regular.");
        }

        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        // *** FIX: Get EmployeeShift from the correct context ***
        int dailyThreshold = getDailyThresholdInMinutes(overtimeSettings, (EmployeeShift) context.getFact("shift"));

        int regularMinutes = Math.min(workedMinutes, dailyThreshold);
        int remainingMinutesForOt = Math.max(0, workedMinutes - regularMinutes);

        context.getFacts().put("regularMinutes", regularMinutes);
        context.getFacts().put("workedMinutes", remainingMinutesForOt); // Pass remaining minutes down the pipeline.

        String message = String.format("Categorized %d minutes as REGULAR time based on a threshold of %d minutes.", regularMinutes, dailyThreshold);
        return buildResult("REGULAR_HOURS_CALCULATED", true, message);
    }

    private int getDailyThresholdInMinutes(OvertimeRules overtimeSettings, EmployeeShift employeeShift) {
        if (overtimeSettings.getDailyOtTrigger() == com.wfm.experts.setup.wfm.paypolicy.enums.DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            return (int) (shiftDuration < 0 ? shiftDuration + 1440 : shiftDuration);
        } else {
            return (overtimeSettings.getThresholdHours() != null ? overtimeSettings.getThresholdHours() * 60 : 0) +
                    (overtimeSettings.getThresholdMinutes() != null ? overtimeSettings.getThresholdMinutes() : 0);
        }
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
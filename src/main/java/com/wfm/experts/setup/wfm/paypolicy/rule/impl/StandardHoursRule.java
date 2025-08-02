package com.wfm.experts.setup.wfm.paypolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.entity.OvertimeRules;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.paypolicy.enums.DailyOtTrigger;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import java.time.Duration;

public class StandardHoursRule implements PayPolicyRule {

    @Override
    public String getName() {
        return "StandardHoursRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        return workedMinutes != null && workedMinutes > 0;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        PayPolicy payPolicy = context.getPayPolicy();

        int standardMinutes;
        int minutesBeyondStandard;

        // Scheduled Employee Logic
        if (employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440; // Handle overnight shift
            }
            standardMinutes = Math.min(workedMinutes, (int) shiftDuration);
            minutesBeyondStandard = Math.max(0, workedMinutes - (int) shiftDuration);

            // Unscheduled Employee with Fixed OT Threshold Logic
        } else if (payPolicy != null && payPolicy.getOvertimeRules() != null && payPolicy.getOvertimeRules().getDailyOtTrigger() == DailyOtTrigger.AFTER_FIXED_HOURS) {
            OvertimeRules otRules = payPolicy.getOvertimeRules();
            int dailyThreshold = (otRules.getThresholdHours() != null ? otRules.getThresholdHours() * 60 : 0) + (otRules.getThresholdMinutes() != null ? otRules.getThresholdMinutes() : 0);

            standardMinutes = Math.min(workedMinutes, dailyThreshold);
            minutesBeyondStandard = Math.max(0, workedMinutes - dailyThreshold);

            // Fallback for Unscheduled Employees (no fixed threshold)
        } else {
            standardMinutes = workedMinutes;
            minutesBeyondStandard = 0;
        }

        context.getFacts().put("finalRegularMinutes", standardMinutes);
        context.getFacts().put("workedMinutes", minutesBeyondStandard); // Pass only the remainder for OT calculation

        String message = String.format("Time partitioned: %d minutes as standard, %d minutes for OT calculation.", standardMinutes, minutesBeyondStandard);
        return buildResult("TIME_PARTITIONED", true, message);
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
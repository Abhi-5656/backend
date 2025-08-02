package com.wfm.experts.setup.wfm.paypolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
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

        int minutesWithinShift = 0;
        int minutesBeyondShift = 0;

        if (employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440; // Handle overnight shift
            }

            minutesWithinShift = Math.min(workedMinutes, (int) shiftDuration);
            minutesBeyondShift = Math.max(0, workedMinutes - (int) shiftDuration);
        } else {
            // If no shift, all time is considered "within shift" for calculation purposes
            minutesWithinShift = workedMinutes;
            minutesBeyondShift = 0;
        }

        context.getFacts().put("minutesWithinShift", minutesWithinShift);
        context.getFacts().put("workedMinutes", minutesBeyondShift); // Pass only excess time to the next rule

        String message = String.format("Categorized time: %d minutes within shift, %d minutes beyond shift.", minutesWithinShift, minutesBeyondShift);
        return buildResult("TIME_CATEGORIZED", true, message);
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
package com.wfm.experts.setup.wfm.paypolicy.rule.impl;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;

/**
 * Implements the Attendance Rule for Pay Policy evaluation.
 * Checks whether the employee is FULL_DAY, HALF_DAY, or ABSENT based on working minutes.
 */
public class AttendanceRuleImpl implements PayPolicyRule {

    private final boolean enabled;
    private final int fullDayMinutes;
    private final int halfDayMinutes;

    /**
     * Constructor for AttendanceRuleImpl.
     *
     * @param enabled         whether attendance rule is enabled
     * @param fullDayHours    hours threshold for full day
     * @param fullDayMinutes  minutes threshold for full day
     * @param halfDayHours    hours threshold for half day
     * @param halfDayMinutes  minutes threshold for half day
     */
    public AttendanceRuleImpl(boolean enabled, Integer fullDayHours, Integer fullDayMinutes, Integer halfDayHours, Integer halfDayMinutes) {
        this.enabled = enabled;
        this.fullDayMinutes = (fullDayHours != null ? fullDayHours : 0) * 60 + (fullDayMinutes != null ? fullDayMinutes : 0);
        this.halfDayMinutes = (halfDayHours != null ? halfDayHours : 0) * 60 + (halfDayMinutes != null ? halfDayMinutes : 0);
    }

    @Override
    public String getName() {
        return "AttendanceRule";
    }

    /**
     * Evaluates whether this rule should run based on the context and if it is enabled.
     */
    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabled;
    }

    /**
     * Executes the attendance rule and returns the attendance status in a PayPolicyRuleResultDTO.
     */
    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = null;
        Object val = context.getFact("workedMinutes");
        if (val instanceof Integer) {
            workedMinutes = (Integer) val;
        }

        String attendanceStatus = "ABSENT";
        if (workedMinutes != null) {
            if (workedMinutes >= fullDayMinutes) {
                attendanceStatus = "FULL_DAY";
            } else if (workedMinutes >= halfDayMinutes) {
                attendanceStatus = "HALF_DAY";
            }
        }

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(attendanceStatus)
                .success(true)
                .message("Attendance evaluated for workedMinutes=" + workedMinutes)
                .build();
    }
}

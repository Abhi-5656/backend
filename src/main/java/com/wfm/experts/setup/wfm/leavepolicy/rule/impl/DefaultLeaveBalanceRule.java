package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import org.springframework.stereotype.Component;

@Component
public class DefaultLeaveBalanceRule implements LeavePolicyRule {

    @Override
    public String getName() {
        return "DefaultLeaveBalanceRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        // This rule acts as a fallback, so it should always be ready to execute.
        return true;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        double balance = 0;
        String message = "No applicable grant configuration found.";

        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();

        if (grantsConfig != null) {
            FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
            // Case 1: Handle "Fixed Grant - One Time" configuration
            if (fixedGrant != null && fixedGrant.getOneTimeDetails() != null) {
                balance = fixedGrant.getOneTimeDetails().getMaxDays();
                message = "Default one-time grant balance applied.";
                // Case 2: Handle "Fixed Grant - Repeatedly" configuration
            } else if (fixedGrant != null && fixedGrant.getRepeatedlyDetails() != null) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear();
                message = "Default repeatedly grant balance applied.";
                // Future Case 3: Handle "Earned Grant" configuration
            } else if (grantsConfig.getEarnedGrant() != null) {
                // For now, earned leave might start at 0, but you can add logic here.
                balance = 0;
                message = "Default earned leave balance applied (starts at 0).";
            }
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }
}
// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/DefaultLeaveBalanceRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
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
        // This rule now ONLY applies to "One Time" grants.
        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
        return grantsConfig != null &&
                grantsConfig.getFixedGrant() != null &&
                grantsConfig.getFixedGrant().getFrequency() == GrantFrequency.ONE_TIME;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        double balance = 0;
        String message = "No applicable one-time grant configuration found.";

        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();

        if (grantsConfig != null) {
            FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
            if (fixedGrant != null && fixedGrant.getOneTimeDetails() != null) {
                balance = fixedGrant.getOneTimeDetails().getMaxDays();
                message = "Default one-time grant balance applied.";
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
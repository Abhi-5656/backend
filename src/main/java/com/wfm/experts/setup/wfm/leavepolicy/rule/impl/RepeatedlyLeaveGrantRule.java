//package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;
//
//import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
//import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Component
//public class RepeatedlyLeaveGrantRule implements LeavePolicyRule {
//
//    @Override
//    public String getName() {
//        return "RepeatedlyLeaveGrantRule";
//    }
//
//    @Override
//    public boolean evaluate(LeavePolicyExecutionContext context) {
//        LeavePolicy leavePolicy = context.getLeavePolicy();
//        return leavePolicy.getGrantsConfig() != null &&
//                leavePolicy.getGrantsConfig().getFixedGrant() != null &&
//                leavePolicy.getGrantsConfig().getFixedGrant().getFrequency() == GrantFrequency.REPEATEDLY;
//    }
//
//    @Override
//    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
//        double balance = 0;
//        String message = "No applicable grant configuration found for Repeatedly grant.";
//
//        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
//        FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
//
//        if (fixedGrant.getRepeatedlyDetails() != null) {
//            balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth();
//            message = "Repeatedly grant balance of " + balance + " days applied.";
//        }
//
//        return LeavePolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result(true)
//                .message(message)
//                .balance(balance)
//                .build();
//    }
//}
// Save as: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/RepeatedlyLeaveGrantRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import org.springframework.stereotype.Component;

@Component
public class RepeatedlyLeaveGrantRule implements LeavePolicyRule {

    @Override
    public String getName() {
        return "RepeatedlyLeaveGrantRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        return leavePolicy.getGrantsConfig() != null &&
                leavePolicy.getGrantsConfig().getFixedGrant() != null &&
                leavePolicy.getGrantsConfig().getFixedGrant().getFrequency() == GrantFrequency.REPEATEDLY;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        double balance = 0;
        String message = "No applicable grant configuration found for Repeatedly grant.";

        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
        FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();

        if (fixedGrant.getRepeatedlyDetails() != null) {
            if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.MONTHLY) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth();
                message = "Repeatedly (monthly) grant balance of " + balance + " days applied.";
            } else if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.YEARLY) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear();
                message = "Repeatedly (yearly) grant balance of " + balance + " days applied.";
            } else if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.PAY_PERIOD) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerPayPeriod();
                message = "Repeatedly (pay period) grant balance of " + balance + " days applied.";
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
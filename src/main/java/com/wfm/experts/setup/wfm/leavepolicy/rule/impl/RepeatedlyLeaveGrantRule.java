////package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;
////
////import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
////import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
////import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
////import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
////import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
////import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
////import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
////import org.springframework.stereotype.Component;
////
////import java.time.LocalDate;
////
////@Component
////public class RepeatedlyLeaveGrantRule implements LeavePolicyRule {
////
////    @Override
////    public String getName() {
////        return "RepeatedlyLeaveGrantRule";
////    }
////
////    @Override
////    public boolean evaluate(LeavePolicyExecutionContext context) {
////        LeavePolicy leavePolicy = context.getLeavePolicy();
////        return leavePolicy.getGrantsConfig() != null &&
////                leavePolicy.getGrantsConfig().getFixedGrant() != null &&
////                leavePolicy.getGrantsConfig().getFixedGrant().getFrequency() == GrantFrequency.REPEATEDLY;
////    }
////
////    @Override
////    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
////        double balance = 0;
////        String message = "No applicable grant configuration found for Repeatedly grant.";
////
////        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
////        FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
////
////        if (fixedGrant.getRepeatedlyDetails() != null) {
////            balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth();
////            message = "Repeatedly grant balance of " + balance + " days applied.";
////        }
////
////        return LeavePolicyRuleResultDTO.builder()
////                .ruleName(getName())
////                .result(true)
////                .message(message)
////                .balance(balance)
////                .build();
////    }
////}
//// Save as: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/RepeatedlyLeaveGrantRule.java
//package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;
//
//import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
//import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
//import org.springframework.stereotype.Component;
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
//            if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.MONTHLY) {
//                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth();
//                message = "Repeatedly (monthly) grant balance of " + balance + " days applied.";
//            } else if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.YEARLY) {
//                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear();
//                message = "Repeatedly (yearly) grant balance of " + balance + " days applied.";
//            } else if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.PAY_PERIOD) {
//                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerPayPeriod();
//                message = "Repeatedly (pay period) grant balance of " + balance + " days applied.";
//            }
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


// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/RepeatedlyLeaveGrantRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RepeatedlyLeaveGrantRule implements LeavePolicyRule {

    @Override
    public String getName() {
        return "RepeatedlyLeaveGrantRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        GrantsConfig grantsConfig = leavePolicy.getGrantsConfig();

        if (grantsConfig == null ||
                grantsConfig.getFixedGrant() == null ||
                grantsConfig.getFixedGrant().getFrequency() != GrantFrequency.REPEATEDLY) {
            return false;
        }

        // *** BUG FIX START ***
        // Do not run this rule for yearly grants in the hiring year if proration is applicable.
        if (grantsConfig.getFixedGrant().getRepeatedlyDetails() != null &&
                grantsConfig.getFixedGrant().getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.YEARLY &&
                leavePolicy.getCalculationDateConfig() != null &&
                leavePolicy.getCalculationDateConfig().getCalculationType() == CalculationDateType.CUSTOM_DATE) {

            LocalDate hireDate = context.getEmployee().getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
            if (context.getProcessingMonth().getYear() == hireDate.getYear()) {
                return false; // Let the Prorata rule handle the hiring year
            }
        }
        // *** BUG FIX END ***

        return true;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        double balance = 0;
        String message = "No applicable grant configuration found for Repeatedly grant.";

        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
        FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();

        if (fixedGrant.getRepeatedlyDetails() != null) {
            GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
            if (grantPeriod == GrantPeriod.MONTHLY) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth();
                message = "Repeatedly (monthly) grant balance of " + balance + " days applied.";
            } else if (grantPeriod == GrantPeriod.YEARLY) {
                balance = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear();
                message = "Repeatedly (yearly) grant balance of " + balance + " days applied.";
            } else if (grantPeriod == GrantPeriod.PAY_PERIOD) {
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
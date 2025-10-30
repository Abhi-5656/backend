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
import com.wfm.experts.setup.wfm.leavepolicy.entity.ProrationConfig; // <-- IMPORT
import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.enums.ProrationCutoffUnit; // <-- IMPORT
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import com.wfm.experts.tenant.common.employees.entity.Employee; // <-- IMPORT
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth; // <-- IMPORT

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

        return true;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        double balance = 0;
        String message = "No applicable grant configuration found for Repeatedly grant.";

        GrantsConfig grantsConfig = context.getLeavePolicy().getGrantsConfig();
        FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
        Employee employee = context.getEmployee(); // Get employee
        YearMonth processingMonth = context.getProcessingMonth(); // Get month

        if (fixedGrant.getRepeatedlyDetails() != null) {
            GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();

            if (grantPeriod == GrantPeriod.MONTHLY) {
                double fullGrantAmount = fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth() != null ?
                        fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth() : 0;

                ProrationConfig prorationConfig = fixedGrant.getRepeatedlyDetails().getProrationConfig();

                // Check for first-month proration (as requested)
                if (isFirstAccrual(employee, processingMonth) &&
                        prorationConfig != null &&
                        prorationConfig.isEnabled() &&
                        prorationConfig.getCutoffUnit() == ProrationCutoffUnit.DAY_OF_MONTH) {

                    LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
                    int joiningDay = hireDate.getDayOfMonth();
                    int cutoffDay = prorationConfig.getCutoffValue() != null ? prorationConfig.getCutoffValue() : 15; // Default to 15

                    double percentageToGrant;
                    if (joiningDay <= cutoffDay) {
                        // Get 'before' percentage, default to 100% if not set
                        percentageToGrant = prorationConfig.getGrantPercentageBeforeCutoff() != null ?
                                prorationConfig.getGrantPercentageBeforeCutoff() : 100.0;
                        message = "Prorated first monthly grant (Joined on/before day " + cutoffDay + "). Granting " + percentageToGrant + "%.";
                    } else {
                        // --- THIS IS THE FIX ---
                        // Get 'after' percentage (your 50%), default to 0% if not set
                        percentageToGrant = prorationConfig.getGrantPercentageAfterCutoff() != null ?
                                prorationConfig.getGrantPercentageAfterCutoff() : 0.0;
                        // --- END OF FIX ---
                        message = "Prorated first monthly grant (Joined after day " + cutoffDay + "). Granting " + percentageToGrant + "%.";
                    }
                    balance = fullGrantAmount * (percentageToGrant / 100.0);

                } else {
                    // Not first month or no proration, grant full amount
                    balance = fullGrantAmount;
                    message = "Repeatedly (monthly) grant balance of " + balance + " days applied.";
                }

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

    /**
     * Helper to check if the processing month is the employee's first accrual month.
     */
    private boolean isFirstAccrual(Employee employee, YearMonth processingMonth) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        return YearMonth.from(hireDate).equals(processingMonth);
    }
}
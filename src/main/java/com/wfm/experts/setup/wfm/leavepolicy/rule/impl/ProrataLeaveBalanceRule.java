//package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;
//
//import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.ProrationConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantType;
//import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Component
//public class ProrataLeaveBalanceRule implements LeavePolicyRule {
//
//    @Override
//    public String getName() {
//        return "ProrataLeaveBalanceRule";
//    }
//
//    @Override
//    public boolean evaluate(LeavePolicyExecutionContext context) {
//        LeavePolicy leavePolicy = context.getLeavePolicy();
//        GrantsConfig grantsConfig = leavePolicy.getGrantsConfig();
//
//        if (grantsConfig != null && grantsConfig.getGrantType() == GrantType.FIXED && grantsConfig.getFixedGrant() != null) {
//            FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
//            if (fixedGrant.getRepeatedlyDetails() != null && fixedGrant.getRepeatedlyDetails().getProrationConfig() != null) {
//                return fixedGrant.getRepeatedlyDetails().getProrationConfig().isEnabled();
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
//        LeavePolicy leavePolicy = context.getLeavePolicy();
//        Employee employee = context.getEmployee();
//        double balance = 0;
//        String message = "Prorata calculation did not apply.";
//
//        FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
//        ProrationConfig prorationConfig = fixedGrant.getRepeatedlyDetails().getProrationConfig();
//        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
//
//        double totalLeaves = 0;
//        if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.YEARLY) {
//            totalLeaves = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear() != null ?
//                    fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear() : 0;
//        } else if (fixedGrant.getRepeatedlyDetails().getGrantPeriod() == GrantPeriod.MONTHLY) {
//            totalLeaves = (fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth() != null ?
//                    fixedGrant.getRepeatedlyDetails().getMaxDaysPerMonth() : 0) * 12;
//        }
//
//
//        if (prorationConfig.getCutoffDay() != null && hireDate.getDayOfMonth() <= prorationConfig.getCutoffDay()) {
//            balance = totalLeaves * (prorationConfig.getGrantPercentageBeforeCutoff() / 100.0);
//            message = "Prorated balance calculated based on hire date before or on cutoff day.";
//        } else {
//            balance = totalLeaves * (prorationConfig.getGrantPercentageAfterCutoff() / 100.0);
//            message = "Prorated balance calculated based on hire date after cutoff day.";
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

// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/ProrataLeaveBalanceRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class ProrataLeaveBalanceRule implements LeavePolicyRule {

    @Override
    public String getName() {
        return "ProrataLeaveBalanceRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        Employee employee = context.getEmployee();
        YearMonth processingMonth = context.getProcessingMonth();

        // Check if the policy has the necessary configuration for this rule
        if (leavePolicy.getGrantsConfig() == null ||
                leavePolicy.getGrantsConfig().getFixedGrant() == null ||
                leavePolicy.getGrantsConfig().getFixedGrant().getFrequency() != GrantFrequency.REPEATEDLY ||
                leavePolicy.getGrantsConfig().getFixedGrant().getRepeatedlyDetails() == null ||
                leavePolicy.getGrantsConfig().getFixedGrant().getRepeatedlyDetails().getGrantPeriod() != GrantPeriod.YEARLY ||
                leavePolicy.getCalculationDateConfig() == null ||
                leavePolicy.getCalculationDateConfig().getCalculationType() != CalculationDateType.CUSTOM_DATE) {
            return false;
        }

        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        LocalDate customGrantDate = leavePolicy.getCalculationDateConfig().getCustomDate();

        // This rule only applies in the employee's hiring year
        return processingMonth.getYear() == hireDate.getYear() && hireDate.isAfter(customGrantDate.withYear(hireDate.getYear()));
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        Employee employee = context.getEmployee();
        LeavePolicy leavePolicy = context.getLeavePolicy();
        double balance = 0;
        String message = "Prorata calculation did not apply.";

        Double totalLeaves = leavePolicy.getGrantsConfig().getFixedGrant().getRepeatedlyDetails().getMaxDaysPerYear();

        if (totalLeaves != null && totalLeaves > 0) {
            LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();

            // Calculate remaining full months, including the month of joining
            long monthsToGrant = 12 - hireDate.getMonthValue() + 1;

            // Calculate the prorated balance
            balance = totalLeaves / 12.0 * monthsToGrant;
            message = "Prorata balance calculated for " + monthsToGrant + " months in the hiring year.";
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }
}
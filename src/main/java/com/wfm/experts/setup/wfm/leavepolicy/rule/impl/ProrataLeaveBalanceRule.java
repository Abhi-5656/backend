package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.GrantsConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

        if (leavePolicy.getCalculationDateConfig() != null &&
                leavePolicy.getCalculationDateConfig().getCalculationType() == CalculationDateType.CUSTOM_DATE) {

            LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
            LocalDate customGrantDate = leavePolicy.getCalculationDateConfig().getCustomDate();

            LocalDate grantDateInHiringYear = customGrantDate.withYear(hireDate.getYear());

            return hireDate.isAfter(grantDateInHiringYear);
        }
        return false;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        Employee employee = context.getEmployee();
        double balance = 0;
        String message = "Prorata calculation could not be applied due to missing grant details.";

        GrantsConfig grantsConfig = leavePolicy.getGrantsConfig();

        if (grantsConfig != null) {
            FixedGrantConfig fixedGrant = grantsConfig.getFixedGrant();
            double totalLeaves = 0;

            if (fixedGrant != null && fixedGrant.getOneTimeDetails() != null) {
                totalLeaves = fixedGrant.getOneTimeDetails().getMaxDays();
            } else if (fixedGrant != null && fixedGrant.getRepeatedlyDetails() != null) {
                totalLeaves = fixedGrant.getRepeatedlyDetails().getMaxDaysPerYear();
            }

            if (totalLeaves > 0) {
                LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();

                // Calculate the number of full months remaining in the year.
                long monthsToGrant = 12 - hireDate.getMonthValue();

                balance = (totalLeaves / 12.0) * monthsToGrant;
                message = "Prorata balance calculated successfully for " + monthsToGrant + " months.";
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
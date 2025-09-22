package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EarnedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.AccrualCadence;
import com.wfm.experts.setup.wfm.leavepolicy.enums.PostingType;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class EarnedLeaveBalanceRule implements LeavePolicyRule {

    private final TimesheetRepository timesheetRepository;

    public EarnedLeaveBalanceRule(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }

    @Override
    public String getName() {
        return "EarnedLeaveBalanceRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        return leavePolicy.getGrantsConfig() != null &&
                leavePolicy.getGrantsConfig().getEarnedGrant() != null;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        Employee employee = context.getEmployee();
        EarnedGrantConfig earnedGrant = context.getLeavePolicy().getGrantsConfig().getEarnedGrant();
        double balance = 0;
        String message = "No accrual for today.";

        LocalDate today = LocalDate.now();
        LocalDate accrualDate = getAccrualDate(today, earnedGrant);

        if (today.isEqual(accrualDate)) {
            balance = calculateEarnedLeaveForMonth(employee, earnedGrant, YearMonth.from(today));
            message = "Earned leave accrued for the month.";
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }

    private double calculateEarnedLeaveForMonth(Employee employee, EarnedGrantConfig earnedGrant, YearMonth month) {
        // Assuming 1 day of leave is earned per month if maxDaysPerYear is 12
        double monthlyAccrual = (double) earnedGrant.getMaxDaysPerYear() / 12;
        return monthlyAccrual;
    }


    private LocalDate getAccrualDate(LocalDate today, EarnedGrantConfig earnedGrant) {
        if (earnedGrant.getAccrualCadence() == AccrualCadence.MONTHLY) {
            if (earnedGrant.getPosting() == PostingType.POST_AT_END) {
                return today.withDayOfMonth(today.lengthOfMonth());
            } else { // POST_AT_START
                return today.withDayOfMonth(1);
            }
        }
        return today; // Default to today if cadence is not monthly
    }
}
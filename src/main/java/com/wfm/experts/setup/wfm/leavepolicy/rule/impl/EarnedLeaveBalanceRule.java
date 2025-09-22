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
            if (isFirstAccrual(employee, context.getLeavePolicy()) && earnedGrant.getProrationConfig() != null && earnedGrant.getProrationConfig().isEnabled()) {
                balance = calculateProratedFirstGrant(employee, earnedGrant);
                message = "Prorated first grant applied.";
            } else {
                balance = calculateRegularAccrual(employee, earnedGrant);
                message = "Regular monthly leave accrued.";
            }
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }

    private double calculateProratedFirstGrant(Employee employee, EarnedGrantConfig earnedGrant) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        long monthsRemaining = 12 - hireDate.getMonthValue();
        double totalLeaves = earnedGrant.getMaxDaysPerYear();
        return (totalLeaves / 12.0) * monthsRemaining;
    }

    private double calculateRegularAccrual(Employee employee, EarnedGrantConfig earnedGrant) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate startOfLastMonth = lastMonth.withDayOfMonth(1);
        LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getEmployeeId(), startOfLastMonth, endOfLastMonth
        );

        long daysWorked = timesheets.stream()
                .filter(ts -> ts.getRegularHoursMinutes() != null && ts.getRegularHoursMinutes() > 0)
                .map(Timesheet::getWorkDate)
                .distinct()
                .count();

        if (earnedGrant.getMaxDaysPerYear() != null && earnedGrant.getMaxDaysPerYear() > 0) {
            double dailyAccrualRate = (double) earnedGrant.getMaxDaysPerYear() / 264; // Assuming 22 working days/month
            return daysWorked * dailyAccrualRate;
        }
        return 0;
    }

    private boolean isFirstAccrual(Employee employee, LeavePolicy leavePolicy) {
        // This is a simplified check. A more robust implementation would check
        // for existing leave balances for this policy.
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        LocalDate today = LocalDate.now();
        return hireDate.getMonth() == today.minusMonths(1).getMonth() && hireDate.getYear() == today.getYear();
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
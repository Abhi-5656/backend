package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
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
    private final PunchEventRepository punchEventRepository;


    public EarnedLeaveBalanceRule(TimesheetRepository timesheetRepository, PunchEventRepository punchEventRepository) {
        this.timesheetRepository = timesheetRepository;
        this.punchEventRepository = punchEventRepository;
    }

    @Override
    public String getName() {
        return "EarnedLeaveBalanceRule";
    }

    @Override
    public boolean evaluate(LeavePolicyExecutionContext context) {
        LeavePolicy leavePolicy = context.getLeavePolicy();
        // This rule only applies to "Earned" leave grants
        return leavePolicy.getGrantsConfig() != null &&
                leavePolicy.getGrantsConfig().getEarnedGrant() != null;
    }

    @Override
    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
        Employee employee = context.getEmployee();
        EarnedGrantConfig earnedGrant = context.getLeavePolicy().getGrantsConfig().getEarnedGrant();
        double balance = 0;
        String message = "Leave not yet accrued for this period.";

        LocalDate today = LocalDate.now();
        LocalDate accrualDate = getAccrualDate(today, earnedGrant);

        // Only run the accrual on the designated accrual day of the month
        if (today.isEqual(accrualDate)) {

            YearMonth monthToAccrue = YearMonth.from(today.minusMonths(1));
            LocalDate startOfMonth = monthToAccrue.atDay(1);
            LocalDate endOfMonth = monthToAccrue.atEndOfMonth();

            // Fetch all punches for the employee within the entire month
            long punchCount = punchEventRepository.countByEmployeeIdAndEventTimeBetween(
                    employee.getEmployeeId(),
                    startOfMonth.atStartOfDay(),
                    endOfMonth.atTime(23, 59, 59)
            );

            // *** YOUR VALIDATION LOGIC ***
            if (punchCount >= 30) {
                if (isFirstAccrual(employee, context.getLeavePolicy()) && earnedGrant.getProrationConfig() != null && earnedGrant.getProrationConfig().isEnabled()) {
                    balance = calculateProratedFirstGrant(employee, earnedGrant);
                    message = "Prorated first grant applied based on punch validation.";
                } else {
                    balance = calculateRegularAccrual(employee, earnedGrant, monthToAccrue);
                    message = "Monthly earned leave accrued after punch validation.";
                }
            } else {
                message = "Leave not accrued. Punch count of " + punchCount + " is below the threshold of 30.";
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

    private double calculateRegularAccrual(Employee employee, EarnedGrantConfig earnedGrant, YearMonth monthToAccrue) {
        LocalDate startOfMonth = monthToAccrue.atDay(1);
        LocalDate endOfMonth = monthToAccrue.atEndOfMonth();

        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getEmployeeId(), startOfMonth, endOfMonth
        );

        long daysWorked = timesheets.stream()
                .filter(ts -> ts.getRegularHoursMinutes() != null && ts.getRegularHoursMinutes() > 0)
                .map(Timesheet::getWorkDate)
                .distinct()
                .count();

        if (earnedGrant.getMaxDaysPerYear() != null && earnedGrant.getMaxDaysPerYear() > 0) {
            // A common practice is to assume a standard number of working days in a year
            double dailyAccrualRate = (double) earnedGrant.getMaxDaysPerYear() / 264; // e.g., 22 working days/month * 12
            return daysWorked * dailyAccrualRate;
        }
        return 0;
    }

    private boolean isFirstAccrual(Employee employee, LeavePolicy leavePolicy) {
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
        // If no specific cadence is defined, default to the last day of the month
        return today.withDayOfMonth(today.lengthOfMonth());
    }
}
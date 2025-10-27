//// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/EarnedLeaveBalanceRule.java
//package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;
//
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.EarnedGrantConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
//import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.List;
//
//@Component
//public class EarnedLeaveBalanceRule implements LeavePolicyRule {
//
//    private final TimesheetRepository timesheetRepository;
//    private final PunchEventRepository punchEventRepository;
//
//
//    public EarnedLeaveBalanceRule(TimesheetRepository timesheetRepository, PunchEventRepository punchEventRepository) {
//        this.timesheetRepository = timesheetRepository;
//        this.punchEventRepository = punchEventRepository;
//    }
//
//    @Override
//    public String getName() {
//        return "EarnedLeaveBalanceRule";
//    }
//
//    @Override
//    public boolean evaluate(LeavePolicyExecutionContext context) {
//        LeavePolicy leavePolicy = context.getLeavePolicy();
//        // This rule only applies to "Earned" leave grants
//        return leavePolicy.getGrantsConfig() != null &&
//                leavePolicy.getGrantsConfig().getEarnedGrant() != null;
//    }
//
//    @Override
//    public LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context) {
//        Employee employee = context.getEmployee();
//        EarnedGrantConfig earnedGrant = context.getLeavePolicy().getGrantsConfig().getEarnedGrant();
//        double balance = 0;
//        String message;
//
//        YearMonth monthToAccrue = context.getProcessingMonth();
//        LocalDate startOfMonth = monthToAccrue.atDay(1);
//        LocalDate endOfMonth = monthToAccrue.atEndOfMonth();
//        int daysInMonth = monthToAccrue.lengthOfMonth();
//
//        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(
//                employee.getEmployeeId(), startOfMonth, endOfMonth
//        );
//
//        long daysWorked = timesheets.stream()
//                .filter(ts -> ts.getRegularHoursMinutes() != null && ts.getRegularHoursMinutes() > 0)
//                .map(Timesheet::getWorkDate)
//                .distinct()
//                .count();
//
//        if (daysWorked >= daysInMonth) {
//            if (isFirstAccrual(employee, context.getProcessingMonth()) && earnedGrant.getProrationConfig() != null && earnedGrant.getProrationConfig().isEnabled()) {
//                balance = calculateProratedFirstGrant(employee, earnedGrant);
//                message = "Prorated first grant applied based on punch validation.";
//            } else {
//                balance = getGrantAmount(earnedGrant);
//                message = "Monthly earned leave accrued after punch validation.";
//            }
//        } else {
//            message = "Leave not accrued. Days worked " + daysWorked + " is below the threshold of " + daysInMonth + ".";
//        }
//
//        return LeavePolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result(true)
//                .message(message)
//                .balance(balance)
//                .build();
//    }
//
//    private double getGrantAmount(EarnedGrantConfig earnedGrant) {
//        if (earnedGrant.getGrantPeriod() == GrantPeriod.MONTHLY && earnedGrant.getMaxDaysPerMonth() != null) {
//            return earnedGrant.getMaxDaysPerMonth();
//        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.YEARLY && earnedGrant.getMaxDaysPerYear() != null) {
//            return earnedGrant.getMaxDaysPerYear() / 12.0;
//        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.PAY_PERIOD && earnedGrant.getMaxDaysPerPayPeriod() != null) {
//            return earnedGrant.getMaxDaysPerPayPeriod();
//        }
//        return 0;
//    }
//
//
//    private double calculateProratedFirstGrant(Employee employee, EarnedGrantConfig earnedGrant) {
//        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
//        long daysInMonth = hireDate.lengthOfMonth();
//        long daysWorked = daysInMonth - hireDate.getDayOfMonth() + 1;
//        double monthlyGrant = 0;
//
//        if (earnedGrant.getGrantPeriod() == GrantPeriod.MONTHLY && earnedGrant.getMaxDaysPerMonth() != null) {
//            monthlyGrant = earnedGrant.getMaxDaysPerMonth();
//        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.YEARLY && earnedGrant.getMaxDaysPerYear() != null) {
//            monthlyGrant = earnedGrant.getMaxDaysPerYear() / 12.0;
//        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.PAY_PERIOD && earnedGrant.getMaxDaysPerPayPeriod() != null) {
//            // This assumes 2 pay periods per month for proration. Adjust if your logic is different.
//            monthlyGrant = earnedGrant.getMaxDaysPerPayPeriod() * 2;
//        }
//
//
//        return (monthlyGrant / daysInMonth) * daysWorked;
//    }
//
//
//    private boolean isFirstAccrual(Employee employee, YearMonth processingMonth) {
//        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
//        return YearMonth.from(hireDate).equals(processingMonth);
//    }
//}

// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/impl/EarnedLeaveBalanceRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule.impl;

import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.entity.EarnedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class EarnedLeaveBalanceRule implements LeavePolicyRule {

    private final TimesheetRepository timesheetRepository;
    private final PunchEventRepository punchEventRepository; // Kept for constructor consistency


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
        String message;

        YearMonth monthToAccrue = context.getProcessingMonth();
        LocalDate startOfMonth = monthToAccrue.atDay(1);
        LocalDate endOfMonth = monthToAccrue.atEndOfMonth();

        // Get the total number of days in the specific month (e.g., 30 for November)
        int daysInMonth = monthToAccrue.lengthOfMonth();

        // --- THIS IS YOUR FIX ---
        // Query the Timesheet table for processed status
        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getEmployeeId(),
                startOfMonth,
                endOfMonth
        );

        // Count the number of distinct days that have a status of "PRESENT"
        long daysWorked = timesheets.stream()
                .filter(ts -> "PRESENT".equalsIgnoreCase(ts.getStatus())) // Granting based on "PRESENT" status
                .map(Timesheet::getWorkDate)
                .distinct()
                .count();
        // --- END OF YOUR FIX ---

        // Check if days worked is equal to the total days in that specific month
        if (daysWorked >= daysInMonth) {

            if (isFirstAccrual(employee, context.getProcessingMonth()) && earnedGrant.getProrationConfig() != null && earnedGrant.getProrationConfig().isEnabled()) {
                balance = calculateProratedFirstGrant(employee, earnedGrant);
                message = "Prorated first grant applied. Worked " + daysWorked + "/" + daysInMonth + " present days.";
            } else {
                balance = getGrantAmount(earnedGrant); // This will be 1.25
                message = "Monthly earned leave accrued. Worked " + daysWorked + "/" + daysInMonth + " present days.";
            }
        } else {
            message = "Leave not accrued. Present days (" + daysWorked + ") is below the threshold of " + daysInMonth + " days.";
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }

    private double getGrantAmount(EarnedGrantConfig earnedGrant) {
        // This logic correctly reads your 1.25
        if (earnedGrant.getGrantPeriod() == GrantPeriod.MONTHLY && earnedGrant.getMaxDaysPerMonth() != null) {
            return earnedGrant.getMaxDaysPerMonth(); // This will be 1.25
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.YEARLY && earnedGrant.getMaxDaysPerYear() != null) {
            return earnedGrant.getMaxDaysPerYear() / 12.0;
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.PAY_PERIOD && earnedGrant.getMaxDaysPerPayPeriod() != null) {
            return earnedGrant.getMaxDaysPerPayPeriod();
        }
        return 0;
    }


    private double calculateProratedFirstGrant(Employee employee, EarnedGrantConfig earnedGrant) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        long daysInMonth = hireDate.lengthOfMonth();
        long daysWorked = daysInMonth - hireDate.getDayOfMonth() + 1;
        double monthlyGrant = 0;

        if (earnedGrant.getGrantPeriod() == GrantPeriod.MONTHLY && earnedGrant.getMaxDaysPerMonth() != null) {
            monthlyGrant = earnedGrant.getMaxDaysPerMonth();
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.YEARLY && earnedGrant.getMaxDaysPerYear() != null) {
            monthlyGrant = earnedGrant.getMaxDaysPerYear() / 12.0;
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.PAY_PERIOD && earnedGrant.getMaxDaysPerPayPeriod() != null) {
            monthlyGrant = earnedGrant.getMaxDaysPerPayPeriod() * 2;
        }


        return (monthlyGrant / daysInMonth) * daysWorked;
    }


    private boolean isFirstAccrual(Employee employee, YearMonth processingMonth) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        return YearMonth.from(hireDate).equals(processingMonth);
    }
}
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

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Component
public class EarnedLeaveBalanceRule implements LeavePolicyRule {

    private final TimesheetRepository timesheetRepository;
    private final PunchEventRepository punchEventRepository; // Kept for constructor consistency
    private final EmployeeShiftRepository employeeShiftRepository;
    private final HolidayProfileAssignmentService holidayProfileAssignmentService; // <-- INJECTED

    public EarnedLeaveBalanceRule(TimesheetRepository timesheetRepository,
                                  PunchEventRepository punchEventRepository,
                                  EmployeeShiftRepository employeeShiftRepository,
                                  HolidayProfileAssignmentService holidayProfileAssignmentService) { // <-- ADDED
        this.timesheetRepository = timesheetRepository;
        this.punchEventRepository = punchEventRepository;
        this.employeeShiftRepository = employeeShiftRepository;
        this.holidayProfileAssignmentService = holidayProfileAssignmentService; // <-- ADDED
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

        // Get the total number of days in the specific month (e.g., 28 for Feb)
        int daysInMonth = monthToAccrue.lengthOfMonth();

        // --- NEW LOGIC V3 ---
        // 1. Get all "PRESENT" days from Timesheets
        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getEmployeeId(), startOfMonth, endOfMonth
        );
        Set<LocalDate> presentDays = timesheets.stream()
                .filter(ts -> "PRESENT".equalsIgnoreCase(ts.getStatus()))
                .map(Timesheet::getWorkDate)
                .collect(Collectors.toSet());

        // 2. Get all "Weekly Off" days from Employee Shifts
        List<EmployeeShift> shifts = employeeShiftRepository.findByEmployeeIdAndCalendarDateBetween(
                employee.getEmployeeId(), startOfMonth, endOfMonth
        );
        Set<LocalDate> weekOffDays = shifts.stream()
                .filter(s -> s.getIsWeekOff() != null && s.getIsWeekOff())
                .map(EmployeeShift::getCalendarDate)
                .collect(Collectors.toSet());

        // 3. Get all "Holiday" days from the Holiday Profile Assignment Service
        List<HolidayDTO> assignedHolidays = holidayProfileAssignmentService.getAssignedHolidaysByEmployeeId(employee.getEmployeeId());

        Set<LocalDate> holidayDates = assignedHolidays.stream()
                .flatMap(holiday -> {
                    // Create a stream of dates from the holiday's start to end
                    // Handle multi-day holidays
                    long daysBetween = ChronoUnit.DAYS.between(holiday.getStartDate(), holiday.getEndDate()) + 1;
                    return LongStream.range(0, daysBetween)
                            .mapToObj(i -> holiday.getStartDate().plusDays(i));
                })
                .filter(date -> !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth)) // Filter for dates within the current month
                .collect(Collectors.toSet());

        // 4. Combine them. A Set automatically handles duplicates.
        Set<LocalDate> countedDays = new HashSet<>(presentDays);
        countedDays.addAll(weekOffDays);
        countedDays.addAll(holidayDates); // Add the holidays from the profile

        long totalCountedDays = countedDays.size();
        // --- END OF NEW LOGIC V3 ---


        // Check if the total "counted" days is equal to or greater than the number of days in the month
        if (totalCountedDays >= daysInMonth) {

            if (isFirstAccrual(employee, context.getProcessingMonth()) && earnedGrant.getProrationConfig() != null && earnedGrant.getProrationConfig().isEnabled()) {
                balance = calculateProratedFirstGrant(employee, earnedGrant, monthToAccrue); // Pass month
                message = "Prorated first grant applied. Counted " + totalCountedDays + "/" + daysInMonth + " days (Present/Off/Holiday).";
            } else {
                balance = getGrantAmount(earnedGrant, monthToAccrue); // Pass month
                message = "Monthly earned leave accrued. Counted " + totalCountedDays + "/" + daysInMonth + " days (Present/Off/Holiday).";
            }
        } else {
            message = "Leave not accrued. Counted days (Present/Off/Holiday) was " + totalCountedDays + ", which is below the threshold of " + daysInMonth + " days.";
        }

        return LeavePolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(true)
                .message(message)
                .balance(balance)
                .build();
    }

    private double getGrantAmount(EarnedGrantConfig earnedGrant, YearMonth monthToAccrue) {
        if (earnedGrant.getGrantPeriod() == GrantPeriod.MONTHLY && earnedGrant.getMaxDaysPerMonth() != null) {
            return earnedGrant.getMaxDaysPerMonth();
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.YEARLY && earnedGrant.getMaxDaysPerYear() != null) {
            // DYNAMIC CALCULATION: (Total Yearly / Days in Year) * Days in Month
            double yearlyGrant = earnedGrant.getMaxDaysPerYear();
            int daysInThisYear = monthToAccrue.isLeapYear() ? 366 : 365;
            int daysInThisMonth = monthToAccrue.lengthOfMonth();
            return (yearlyGrant / daysInThisYear) * daysInThisMonth;
        } else if (earnedGrant.getGrantPeriod() == GrantPeriod.PAY_PERIOD && earnedGrant.getMaxDaysPerPayPeriod() != null) {
            return earnedGrant.getMaxDaysPerPayPeriod();
        }
        return 0;
    }


    private double calculateProratedFirstGrant(Employee employee, EarnedGrantConfig earnedGrant, YearMonth monthToAccrue) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        long daysInMonth = hireDate.lengthOfMonth();
        long daysWorked = daysInMonth - hireDate.getDayOfMonth() + 1;

        // Get the grant amount for the specific month
        double grantForMonth = getGrantAmount(earnedGrant, monthToAccrue);

        // Prorate based on the days in the hiring month
        return (grantForMonth / daysInMonth) * daysWorked;
    }


    private boolean isFirstAccrual(Employee employee, YearMonth processingMonth) {
        LocalDate hireDate = employee.getOrganizationalInfo().getEmploymentDetails().getDateOfJoining();
        return YearMonth.from(hireDate).equals(processingMonth);
    }
}
// Modify the file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/dashboard/service/impl/DashboardServiceImpl.java
package com.wfm.experts.dashboard.service.impl;

import com.wfm.experts.dashboard.dto.*;
import com.wfm.experts.dashboard.service.DashboardService;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveBalanceService;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TimesheetService timesheetService;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveProfileAssignmentRepository leaveProfileAssignmentRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final HolidayProfileAssignmentService holidayProfileAssignmentService;


    // TODO: inject from policy/profile (per-employee). Defaults keep you moving.
    private static final Set<DayOfWeek> WEEKLY_OFF = Set.of(DayOfWeek.SUNDAY);
    private static final double TARGET_DAILY_HOURS = 8.0; // or 9.0 if that’s your standard

    @Override
    public AttendanceTimesheetDTO getAttendanceTimesheet(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<TimesheetDTO> weeklyTimesheets =
                timesheetService.getTimesheetsByEmployeeAndDateRange(employeeId, startOfWeek, endOfWeek);

        // Sum minutes -> hours per day (if multiple rows for a day, we sum them).
        Map<LocalDate, Double> hoursByDate = weeklyTimesheets.stream()
                .collect(Collectors.groupingBy(
                        TimesheetDTO::getWorkDate,
                        Collectors.summingDouble(ts ->
                                Optional.ofNullable(ts.getTotalWorkDurationMinutes()).orElse(0) / 60.0)
                ));

        // Actual hours so far (<= today)
        double actualHoursSoFar = hoursByDate.entrySet().stream()
                .filter(e -> !e.getKey().isAfter(today))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        // Expected hours so far (<= today), excluding weekly-offs
        double expectedHoursSoFar = 0.0;
        for (LocalDate d = startOfWeek; !d.isAfter(today) && !d.isAfter(endOfWeek); d = d.plusDays(1)) {
            if (!WEEKLY_OFF.contains(d.getDayOfWeek())) {
                expectedHoursSoFar += TARGET_DAILY_HOURS;
            }
        }

        double weeklyProgress = expectedHoursSoFar <= 0
                ? 0
                : Math.min(100.0, (actualHoursSoFar / expectedHoursSoFar) * 100.0);

        AnomalyDTO anomaly = checkForAnomaly(weeklyTimesheets);

        // Build daily hours list for the whole week (Sun..Sat)
        List<DailyHoursDTO> dailyHours = new ArrayList<>();
        for (LocalDate d = startOfWeek; !d.isAfter(endOfWeek); d = d.plusDays(1)) {
            String dayName = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
            double hours = hoursByDate.getOrDefault(d, 0.0);
            dailyHours.add(DailyHoursDTO.builder().day(dayName).hours(hours).build());
        }

        return AttendanceTimesheetDTO.builder()
                .weeklyProgress(weeklyProgress)
                .anomaly(anomaly)
                .dailyHours(dailyHours)
                .build();
    }

    @Override
    public MySummaryDTO getMySummary(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        List<TimesheetDTO> weeklyTimesheets = timesheetService.getTimesheetsByEmployeeAndDateRange(employeeId, startOfWeek, today);
        long daysWorked = weeklyTimesheets.stream().filter(ts -> ts.getTotalWorkDurationMinutes() != null && ts.getTotalWorkDurationMinutes() > 0).count();

        String daysThisWeek = String.format("%.1f / 5", (float)daysWorked);


        Optional<EmployeeShift> todayShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, today);
        String upcomingShift = "No Shift";
        if (todayShift.isPresent() && todayShift.get().getShift() != null) {
            upcomingShift = todayShift.get().getShift().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            Optional<EmployeeShift> tomorrowShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, today.plusDays(1));
            if (tomorrowShift.isPresent() && tomorrowShift.get().getShift() != null) {
                upcomingShift = tomorrowShift.get().getShift().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }

        return MySummaryDTO.builder()
                .pendingRequests(2)
                .daysThisWeek(daysThisWeek)
                .upcomingShift(upcomingShift)
                .build();
    }

    @Override
    public LeaveAndHolidaysDTO getLeaveAndHolidays(String employeeId) {
        List<LeaveBalanceSummaryDTO> leaveBalanceSummaries = getLeaveBalanceSummaries(employeeId);
        List<HolidayDTO> upcomingHolidays = holidayProfileAssignmentService.getAssignedHolidaysByEmployeeId(employeeId)
                .stream()
                .filter(holiday -> !holiday.getStartDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        return LeaveAndHolidaysDTO.builder()
                .leaveBalances(leaveBalanceSummaries)
                .upcomingHolidays(upcomingHolidays)
                .build();
    }

    private List<LeaveBalanceSummaryDTO> getLeaveBalanceSummaries(String employeeId) {
        Optional<LeaveProfileAssignment> assignment = leaveProfileAssignmentRepository.findByEmployeeId(employeeId).stream().findFirst();
        if (assignment.isEmpty()) {
            return Collections.emptyList();
        }

        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.get().getLeaveProfileId()).orElse(null);
        if (leaveProfile == null) {
            return Collections.emptyList();
        }

        List<LeavePolicy> policies = leaveProfile.getLeaveProfilePolicies().stream()
                .map(p -> p.getLeavePolicy()).collect(Collectors.toList());

        List<LeaveBalanceDTO> balances = leaveBalanceService.getLeaveBalances(employeeId);
        Map<String, Double> balanceMap = balances.stream()
                .collect(Collectors.toMap(LeaveBalanceDTO::getLeavePolicyName, LeaveBalanceDTO::getBalance));

        return policies.stream().map(policy -> {
            double total = 0;
            if (policy.getGrantsConfig() != null && policy.getGrantsConfig().getFixedGrant() != null && policy.getGrantsConfig().getFixedGrant().getOneTimeDetails() != null) {
                total = policy.getGrantsConfig().getFixedGrant().getOneTimeDetails().getMaxDays();
            }
            return LeaveBalanceSummaryDTO.builder()
                    .leaveName(policy.getPolicyName())
                    .balance(balanceMap.getOrDefault(policy.getPolicyName(), 0.0))
                    .total(total)
                    .build();
        }).collect(Collectors.toList());
    }

    private AnomalyDTO checkForAnomaly(List<TimesheetDTO> timesheets) {
        for (TimesheetDTO ts : timesheets) {
            if (ts.getPunchEvents() != null && ts.getPunchEvents().size() % 2 != 0) {
                return AnomalyDTO.builder()
                        .message("Missed punch-out")
                        .date(ts.getWorkDate().toString())
                        .build();
            }
        }
        return null;
    }
}
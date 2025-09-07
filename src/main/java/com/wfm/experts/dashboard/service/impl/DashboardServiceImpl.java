package com.wfm.experts.dashboard.service.impl;

import com.wfm.experts.dashboard.dto.AnomalyDTO;
import com.wfm.experts.dashboard.dto.AttendanceTimesheetDTO;
import com.wfm.experts.dashboard.dto.DailyHoursDTO;
import com.wfm.experts.dashboard.dto.MySummaryDTO;
import com.wfm.experts.dashboard.service.DashboardService;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
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


        // Dummy data as requested
        return MySummaryDTO.builder()
                .leaveBalance("12 Days")
                .pendingRequests(2)
                .daysThisWeek(daysThisWeek)
                .upcomingShift(upcomingShift)
                .build();
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

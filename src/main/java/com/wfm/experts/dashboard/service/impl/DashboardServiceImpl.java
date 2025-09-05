package com.wfm.experts.dashboard.service.impl;

import com.wfm.experts.dashboard.dto.AnomalyDTO;
import com.wfm.experts.dashboard.dto.AttendanceTimesheetDTO;
import com.wfm.experts.dashboard.dto.DailyHoursDTO;
import com.wfm.experts.dashboard.service.DashboardService;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TimesheetService timesheetService;

    @Override
    public AttendanceTimesheetDTO getAttendanceTimesheet(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<TimesheetDTO> weeklyTimesheets = timesheetService.getTimesheetsByEmployeeAndDateRange(employeeId, startOfWeek, endOfWeek);

        double totalHoursWorked = weeklyTimesheets.stream()
                .mapToDouble(ts -> (ts.getTotalWorkDurationMinutes() != null ? ts.getTotalWorkDurationMinutes() : 0) / 60.0)
                .sum();

        // Assuming a 40-hour work week for progress calculation.
        double weeklyProgress = (totalHoursWorked / 40.0) * 100;

        AnomalyDTO anomaly = checkForAnomaly(weeklyTimesheets);

        List<DailyHoursDTO> dailyHours = new ArrayList<>();
        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            final LocalDate currentDate = date; // Create an effectively final variable
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
            double hours = weeklyTimesheets.stream()
                    .filter(ts -> ts.getWorkDate().isEqual(currentDate)) // Use the effectively final variable here
                    .mapToDouble(ts -> (ts.getTotalWorkDurationMinutes() != null ? ts.getTotalWorkDurationMinutes() : 0) / 60.0)
                    .findFirst()
                    .orElse(0.0);
            dailyHours.add(DailyHoursDTO.builder().day(dayName).hours(hours).build());
        }

        return AttendanceTimesheetDTO.builder()
                .weeklyProgress(weeklyProgress)
                .anomaly(anomaly)
                .dailyHours(dailyHours)
                .build();
    }

    private AnomalyDTO checkForAnomaly(List<TimesheetDTO> timesheets) {
        for (TimesheetDTO timesheet : timesheets) {
            // An anomaly is detected if there's an odd number of punch events, indicating a missed punch.
            if (timesheet.getPunchEvents() != null && timesheet.getPunchEvents().size() % 2 != 0) {
                return AnomalyDTO.builder()
                        .message("Missed punch-out")
                        .date(timesheet.getWorkDate().toString())
                        .build();
            }
        }
        return null; // No anomalies found
    }
}
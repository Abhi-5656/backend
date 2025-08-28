package com.wfm.experts.dashboard.controller;

import com.wfm.experts.dashboard.dto.TodaysStatusDTO;
import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TimesheetService timesheetService;
    private final EmployeeService employeeService;
    private final PunchEventService punchEventService;

    /**
     * Get the weekly timesheet data for a specific employee.
     *
     * @param employeeId The ID of the employee to get the timesheet for.
     * @return A list of TimesheetDTO objects for the current week.
     */
    @GetMapping("/weekly-timesheet/{employeeId}")
    @PreAuthorize("hasAuthority('timesheet:readAll') or (hasAuthority('timesheet:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<List<TimesheetDTO>> getWeeklyTimesheet(@PathVariable String employeeId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<TimesheetDTO> weeklyTimesheet = timesheetService.getTimesheetsByEmployeeAndDateRange(employeeId, startOfWeek, endOfWeek);
        return ResponseEntity.ok(weeklyTimesheet);
    }

    /**
     * Get the total number of employees.
     *
     * @return The total number of employees.
     */
    @GetMapping("/total-employees")
    @PreAuthorize("hasAuthority('employee:readAll')")
    public ResponseEntity<Integer> getTotalEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees().size());
    }

    /**
     * Create a new punch event for an employee (punch in/out).
     *
     * @param punchEventDTO The punch event data.
     * @return The created punch event.
     */
    @PostMapping("/punch-in-out")
    @PreAuthorize("hasAuthority('timesheet:create') or (hasAuthority('timesheet:create:own') and #punchEventDTO.employeeId == authentication.principal.username)")
    public ResponseEntity<PunchEventDTO> punchInOut(@RequestBody PunchEventDTO punchEventDTO) {
        return ResponseEntity.ok(punchEventService.createPunchEvent(punchEventDTO));
    }

    /**
     * Get today's status for an employee.
     *
     * @param employeeId The ID of the employee to get the status for.
     * @return Today's status, including clock-in time, working hours, and attendance status.
     */
    @GetMapping("/todays-status/{employeeId}")
    @PreAuthorize("hasAuthority('timesheet:readAll') or (hasAuthority('timesheet:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<TodaysStatusDTO> getTodaysStatus(@PathVariable String employeeId) {
        Optional<TimesheetDTO> todaysTimesheetOpt = timesheetService.getTimesheetByEmployeeAndDate(employeeId, LocalDate.now());

        if (todaysTimesheetOpt.isEmpty()) {
            return ResponseEntity.ok(TodaysStatusDTO.builder().status("Absent").build());
        }

        TimesheetDTO todaysTimesheet = todaysTimesheetOpt.get();
        String clockIn = "N/A";
        String workingHours = "0h 0m";

        if (todaysTimesheet.getPunchEvents() != null && !todaysTimesheet.getPunchEvents().isEmpty()) {
            Optional<PunchEventDTO> firstInPunch = todaysTimesheet.getPunchEvents().stream()
                    .filter(p -> p.getPunchType() == PunchType.IN)
                    .min(Comparator.comparing(PunchEventDTO::getEventTime));

            if (firstInPunch.isPresent()) {
                clockIn = firstInPunch.get().getEventTime().format(DateTimeFormatter.ofPattern("h:mm a"));
            }

            if (todaysTimesheet.getTotalWorkDurationMinutes() != null) {
                long hours = todaysTimesheet.getTotalWorkDurationMinutes() / 60;
                long minutes = todaysTimesheet.getTotalWorkDurationMinutes() % 60;
                workingHours = String.format("%dh %dm", hours, minutes);
            }
        }

        TodaysStatusDTO todaysStatus = TodaysStatusDTO.builder()
                .clockIn(clockIn)
                .workingHours(workingHours)
                .status(todaysTimesheet.getStatus())
                .build();

        return ResponseEntity.ok(todaysStatus);
    }
}
package com.wfm.experts.dashboard.controller;

import com.wfm.experts.dashboard.dto.AttendanceTimesheetDTO;
import com.wfm.experts.dashboard.dto.LeaveAndHolidaysDTO;
import com.wfm.experts.dashboard.dto.MySummaryDTO;
import com.wfm.experts.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/attendance-timesheet/{employeeId}")
    @PreAuthorize("hasAuthority('timesheet:readAll') or (hasAuthority('timesheet:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<AttendanceTimesheetDTO> getAttendanceTimesheet(@PathVariable String employeeId) {
        return ResponseEntity.ok(dashboardService.getAttendanceTimesheet(employeeId));
    }

    @GetMapping("/my-summary/{employeeId}")
    @PreAuthorize("hasAuthority('timesheet:readAll') or (hasAuthority('timesheet:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<MySummaryDTO> getMySummary(@PathVariable String employeeId) {
        return ResponseEntity.ok(dashboardService.getMySummary(employeeId));
    }

    @GetMapping("/leave-and-holidays/{employeeId}")
    @PreAuthorize("hasAuthority('timesheet:readAll') or (hasAuthority('timesheet:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<LeaveAndHolidaysDTO> getLeaveAndHolidays(@PathVariable String employeeId) {
        return ResponseEntity.ok(dashboardService.getLeaveAndHolidays(employeeId));
    }
}
package com.wfm.experts.dashboard.service;

import com.wfm.experts.dashboard.dto.AttendanceTimesheetDTO;
import com.wfm.experts.dashboard.dto.MySummaryDTO;

public interface DashboardService {
    AttendanceTimesheetDTO getAttendanceTimesheet(String employeeId);

    MySummaryDTO getMySummary(String employeeId);
}
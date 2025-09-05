package com.wfm.experts.dashboard.service;

import com.wfm.experts.dashboard.dto.AttendanceTimesheetDTO;

public interface DashboardService {
    AttendanceTimesheetDTO getAttendanceTimesheet(String employeeId);
}
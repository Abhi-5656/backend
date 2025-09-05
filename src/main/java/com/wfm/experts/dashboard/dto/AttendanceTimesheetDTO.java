package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceTimesheetDTO {
    private double weeklyProgress;
    private AnomalyDTO anomaly;
    private List<DailyHoursDTO> dailyHours;
}
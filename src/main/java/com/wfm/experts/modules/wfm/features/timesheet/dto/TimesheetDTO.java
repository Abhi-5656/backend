package com.wfm.experts.modules.wfm.features.timesheet.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Timesheet.
 * Represents the timesheet record for an employee on a given date, including a detailed breakdown of work hours,
 * status, punch events, rule evaluation trace, and timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetDTO {

    private Long id;

    private String employeeId;

    private LocalDate workDate;

    /** Regular work duration in minutes */
    private Integer regularHoursMinutes;

//    /** Daily overtime in minutes */
//    private Integer dailyOtHoursMinutes;

    /** Excess hours beyond daily OT limit in minutes */
    private Integer excessHoursMinutes;

//    /** Weekly overtime in minutes */
//    private Integer weeklyOtHoursMinutes;

    /** Total work duration in minutes (sum of all categories) */
    private Integer totalWorkDurationMinutes;

    /** Status of the timesheet (e.g., APPROVED, PENDING) */
    private String status;

    /** Pay policy rule evaluation trace (serialized JSON, if available) */
    private String ruleResultsJson;

    /** When pay policy calculation was last performed */
    private LocalDate calculatedAt;

    /** Punch events for this timesheet (if fetched) */
    private List<PunchEventDTO> punchEvents;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
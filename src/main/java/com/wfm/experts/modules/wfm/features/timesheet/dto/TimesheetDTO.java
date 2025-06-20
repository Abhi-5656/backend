package com.wfm.experts.modules.wfm.features.timesheet.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Timesheet.
 * Represents the timesheet record for an employee on a given date, including summary durations,
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

    /** Total work duration in hours (e.g., 7.5 for 7 hours 30 minutes) */
    private Double totalWorkDuration;

    /** Total work duration in minutes (precise) */
    private Integer workDurationMinutes;

    /** Overtime in hours (e.g., 2.0 for 2 hours overtime) */
    private Integer overtimeDuration;

    /** Status of the timesheet (APPROVED, PENDING, etc.) */
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

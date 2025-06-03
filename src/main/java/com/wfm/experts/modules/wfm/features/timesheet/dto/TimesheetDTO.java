package com.wfm.experts.modules.wfm.features.timesheet.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetDTO {

    private Long id;
    private String employeeId;
    private LocalDate workDate;
    private Double totalWorkDuration;    // in hours
    private Double overtimeDuration;     // in hours
    private String status;               // APPROVED, PENDING, etc.
    private List<PunchEventDTO> punchEvents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

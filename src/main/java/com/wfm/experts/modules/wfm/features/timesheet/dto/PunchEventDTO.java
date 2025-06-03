package com.wfm.experts.modules.wfm.features.timesheet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchEventStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEventDTO {

    private Long id;
    private String employeeId; // <<<< CHANGED HERE
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") // GOOD
    private LocalDateTime eventTime;
    private PunchType punchType;
    private PunchEventStatus status;
    private String deviceId;
    private Double geoLat;
    private Double geoLong;
    private String notes;
    private Long timesheetId;

    // --- Include shiftId only in RESPONSE ---
    // MapStruct will populate this from PunchEvent.getShift().getId() for GET/response payloads
    private Long shiftId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

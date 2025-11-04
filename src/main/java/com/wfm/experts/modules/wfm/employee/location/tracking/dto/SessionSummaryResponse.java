package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SessionSummaryResponse {
    private Long sessionId;
    private String employeeId;
    private String status;
    private String clockInTime;     // ISO
    private String clockOutTime;    // ISO (nullable)
    private int totalPoints;
    private double totalDistanceM;
    private String pathGeoJson;     // null if still OPEN or not merged
    private String bboxGeoJson;     // envelope of path (if present)
}

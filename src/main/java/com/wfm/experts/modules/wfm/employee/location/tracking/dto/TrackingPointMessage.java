package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingPointMessage {
    private Long sessionId;
    private String employeeId;
    private long seq;
    private Double lat;
    private Double lng;
    private String capturedAt;
    private String tenantId;
}
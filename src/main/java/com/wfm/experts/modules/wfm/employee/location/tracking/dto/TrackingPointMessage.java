// TrackingPointMessage.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.*;

import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingPointMessage {
    private Long sessionId;
    private String employeeId;
    private Long seq;          // 0,1,2...
    private Double lat;
    private Double lng;
    private Instant capturedAt; // matches your DTOs
}

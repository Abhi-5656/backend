// com/wfm/experts/modules/wfm/employee/location/tracking/dto/LiveResponse.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.*;

import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LiveResponse {
    private Long sessionId;
    private Double currentLat;
    private Double currentLng;
    private Instant clockInTime;
    private Integer totalPoints;
    private Double totalDistanceM;     // approx; computed server-side
    private String polylineGeoJson;    // optional; null if not built
}

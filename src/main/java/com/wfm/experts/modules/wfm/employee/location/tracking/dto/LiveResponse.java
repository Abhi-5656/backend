package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveResponse {
    private Long sessionId;
    private String polylineGeoJson;
    private Double currentLat;
    private Double currentLng;
    private String clockInTime;
    private Integer totalPoints;
    private Double totalDistanceM;
}

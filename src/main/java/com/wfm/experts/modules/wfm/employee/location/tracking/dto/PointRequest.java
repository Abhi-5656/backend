// com/wfm/experts/modules/wfm/employee/location/tracking/dto/PointRequest.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointRequest {
    @NotNull private Long sessionId;
    @NotNull private Double lat;
    @NotNull private Double lng;
    @NotNull private Instant capturedAt;  // ISO8601 UTC
    @NotNull private Integer seq;         // client’s monotonic counter
}

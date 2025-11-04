// com/wfm/experts/modules/wfm/employee/location/tracking/dto/ClockInRequest.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClockInRequest {
    @NotNull private Double lat;
    @NotNull private Double lng;
    @NotNull private Instant capturedAt;  // from Flutter: ISO8601 UTC
}

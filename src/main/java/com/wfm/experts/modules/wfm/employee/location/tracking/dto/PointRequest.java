package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointRequest {
    @NotNull private Long sessionId;
    @NotNull private Double lat;
    @NotNull private Double lng;
    @NotBlank private String capturedAt;
    @Min(0) private long seq;
}
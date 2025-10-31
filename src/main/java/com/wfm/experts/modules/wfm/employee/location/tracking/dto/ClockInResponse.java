package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClockInResponse {
    private Long sessionId;  // numeric, matches your new frontend
    private String status;   // "OPEN"
}

// ClockOutRequest.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClockOutRequest {
    @NotNull private Long sessionId;
    @NotNull private Integer seq; // last seq sent by client when clocking out
}

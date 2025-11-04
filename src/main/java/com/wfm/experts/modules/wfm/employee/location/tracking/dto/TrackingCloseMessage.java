// TrackingCloseMessage.java
package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingCloseMessage {
    private Long sessionId;
    private String employeeId;
    private Long seq; // last seq seen from client
}

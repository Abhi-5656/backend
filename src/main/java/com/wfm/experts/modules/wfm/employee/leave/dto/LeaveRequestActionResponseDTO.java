package com.wfm.experts.modules.wfm.employee.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestActionResponseDTO {
    private Long leaveRequestId;
    private String newStatus;
    private String message;
}
package com.wfm.experts.modules.wfm.employee.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestApprovalDTO {
    private Long approvalId;
    private Long leaveRequestId;
    private String employeeId;
    private String employeeName;
    private String leavePolicyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double leaveDays;
    private String reason;
    private int approvalLevel;
    private String status;
}
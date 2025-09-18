package com.wfm.experts.modules.wfm.employee.leave.dto;

import com.wfm.experts.modules.wfm.employee.leave.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {
    private Long id;
    private String employeeId;
    private Long leavePolicyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double leaveDays;
    private String reason;
    private LeaveStatus status;
}
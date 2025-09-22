package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveBalanceSummaryDTO {
    private Long leavePolicyId;
    private String leaveName;
    private double balance;
    private double total;
}
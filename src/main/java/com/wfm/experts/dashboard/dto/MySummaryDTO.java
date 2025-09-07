package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MySummaryDTO {
    private String leaveBalance;
    private int pendingRequests;
    private String daysThisWeek;
    private String upcomingShift;
}
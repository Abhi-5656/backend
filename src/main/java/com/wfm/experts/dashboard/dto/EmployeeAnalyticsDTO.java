package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeAnalyticsDTO {
    private long newHires;
    private long departures;
}
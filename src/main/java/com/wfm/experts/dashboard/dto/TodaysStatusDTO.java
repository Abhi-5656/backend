package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TodaysStatusDTO {
    private String clockIn;
    private String workingHours;
    private String status;
    private String clockOut;// Added field
}
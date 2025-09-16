package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.AccrualCadence;
import com.wfm.experts.setup.wfm.leavepolicy.enums.PostingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EarnedGrantConfigDto {
    private Integer maxDaysPerYear;
    private Double ratePerPeriod;
    private Integer maxConsecutiveDays;
    private AccrualCadence accrualCadence;
    private PostingType posting;
    private Integer minAdvanceNoticeInDays;
    private ProrationConfigDto prorationConfig;
}
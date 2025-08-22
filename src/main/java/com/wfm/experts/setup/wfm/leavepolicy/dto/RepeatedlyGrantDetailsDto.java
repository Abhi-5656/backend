package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepeatedlyGrantDetailsDto {
    private Integer maxDaysPerYear;
    private Integer maxDaysPerMonth;
    private Integer minAdvanceNoticeInDays;
    private Integer minWorkedBeforeGrantInDays;
}
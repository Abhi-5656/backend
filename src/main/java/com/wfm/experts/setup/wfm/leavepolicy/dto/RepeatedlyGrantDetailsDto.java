// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/RepeatedlyGrantDetailsDto.java
package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
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
public class RepeatedlyGrantDetailsDto {
    private Double maxDaysPerYear;
    private Double maxDaysPerMonth;
    private Double maxDaysPerPayPeriod; // Added field
    private GrantPeriod grantPeriod; // Added field
    private PostingType posting; // Added field
    private Integer minAdvanceNoticeInDays;
    private Integer minWorkedBeforeGrantInDays;
    private ProrationConfigDto prorationConfig;
}
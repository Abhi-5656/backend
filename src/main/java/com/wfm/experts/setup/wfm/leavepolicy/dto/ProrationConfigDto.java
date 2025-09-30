// Save as: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/ProrationConfigDto.java
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
public class ProrationConfigDto {
    private boolean isEnabled;
    private Integer cutoffDay;
    private Integer grantPercentageBeforeCutoff;
    private Integer grantPercentageAfterCutoff;
}
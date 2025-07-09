// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/ConditionalRuleDTO.java
package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.wfm.experts.setup.wfm.leavepolicy.enums.OccurrencePeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a conditional rule that can override the base leave policy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionalRuleDTO {
    private Long id;
    private Integer tenure;
    private Integer overrideMax;
    private Integer overrideMinNotice;
    private Integer overrideMinWorked;
    private Integer overrideOccurrenceLimit;
    private OccurrencePeriod overrideOccurrencePeriod;
}

package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundingRuleDTO {
    private Long id;
    private Integer interval;
    private RoundingType type;
    private Integer gracePeriod;
}

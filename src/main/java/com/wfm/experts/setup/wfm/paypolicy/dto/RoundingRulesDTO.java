package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundingRulesDTO {
    private Long id;
    private boolean enabled;
    private RoundingRuleScope scope;
    private RoundingRuleDTO clockIn;
    private RoundingRuleDTO clockOut;
}

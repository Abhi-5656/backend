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

    // --- NEW FIELDS ---
    // Defines the window (in minutes) around a shift's start/end time
    // during which rounding rules should be applied.
    private Integer applyBeforeShiftMinutes;
    private Integer applyAfterShiftMinutes;
}

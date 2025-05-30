package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEventRulesDTO {
    private Long id;
    private boolean enabled;
    private Integer earlyIn;
    private Integer lateIn;
    private Integer earlyOut;
    private Integer lateOut;
    private boolean notifyOnPunchEvents;
}

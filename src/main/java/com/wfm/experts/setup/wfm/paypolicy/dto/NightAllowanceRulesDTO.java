package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NightAllowanceRulesDTO {
    private Long id;
    private boolean enabled;
    private String startTime;
    private String endTime;
    private Double payMultiplier;
}
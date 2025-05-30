package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreShiftInclusionDTO {
    private Long id;
    private boolean enabled;
    private Integer fromValue;
    private String fromUnit;
    private Integer upToValue;
    private String upToUnit;
}

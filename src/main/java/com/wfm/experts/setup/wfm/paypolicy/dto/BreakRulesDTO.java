package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakRulesDTO {
    private Long id;
    private boolean enabled;
    private boolean allowMultiple;
    private List<BreakDTO> breaks;
}

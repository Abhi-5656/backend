package com.wfm.experts.setup.wfm.shift.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeekPatternDTO {
    private Integer week; // 1-based
    private List<ShiftRotationDayDTO> days;
}

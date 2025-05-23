package com.wfm.experts.setup.wfm.shift.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDTO {
    private Long id;
    private String name;
    private Integer weeks;
    private List<WeekPatternDTO> weeksPattern;
    private Boolean isActive;
}

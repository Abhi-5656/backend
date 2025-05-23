package com.wfm.experts.setup.wfm.shift.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDayDTO {
    private String weekday;
    private ShiftDTO shift;
}

package com.wfm.experts.setup.wfm.shift.dto;

import com.wfm.experts.setup.wfm.shift.enums.Weekday;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDayDTO {
//    private String weekday;
    private Weekday weekday;     // <-- Enum, not String
    private ShiftDTO shift; // nullable if weekOff is true
    private Boolean weekOff;
}

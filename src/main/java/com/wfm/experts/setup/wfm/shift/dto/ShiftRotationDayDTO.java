package com.wfm.experts.setup.wfm.shift.dto;

import com.wfm.experts.setup.wfm.shift.enums.Weekday;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDayDTO {

    @NotNull(message = "Weekday cannot be null")
    private Weekday weekday;

    private ShiftDTO shift; // Can be null if it's a week off

    private Boolean weekOff;
}
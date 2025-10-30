package com.wfm.experts.setup.wfm.shift.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeekPatternDTO {
    @NotNull(message = "Week number cannot be null")
    @Min(value = 1, message = "Week number must be at least 1")
    private Integer week;

    @NotNull(message = "Days list cannot be null")
    private List<@Valid ShiftRotationDayDTO> days;
}
package com.wfm.experts.setup.wfm.shift.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDTO {
    private Long id;

    @NotBlank(message = "Rotation name cannot be blank")
    private String name;

    @NotNull(message = "Number of weeks cannot be null")
    @Min(value = 1, message = "Weeks must be at least 1")
    private Integer weeks;

    @NotEmpty(message = "Weeks pattern cannot be empty")
    private List<@Valid WeekPatternDTO> weeksPattern;

    private Boolean isActive;
}
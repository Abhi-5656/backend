package com.wfm.experts.setup.wfm.shift.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShiftDTO {
    private Long id;

    @NotBlank(message = "Shift name cannot be blank")
    private String shiftName;

    @NotBlank(message = "Shift label cannot be blank")
    private String shiftLabel;

    private String color;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format")
    private String endTime;

    private Boolean isActive;

    @JsonIgnore
    private String createdAt;

    @JsonIgnore
    private String updatedAt;
}
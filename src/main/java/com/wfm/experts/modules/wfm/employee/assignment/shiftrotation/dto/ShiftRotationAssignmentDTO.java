package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRotationAssignmentDTO {

    @NotBlank(message = "Employee ID cannot be blank.")
    private String employeeId;

    @NotNull(message = "Shift Rotation ID cannot be null.")
    private Long shiftRotationId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;
}
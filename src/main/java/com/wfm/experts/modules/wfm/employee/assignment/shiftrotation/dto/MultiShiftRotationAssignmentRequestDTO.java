package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiShiftRotationAssignmentRequestDTO {

    @NotEmpty(message = "Employees list cannot be null or empty.")
    private List<String> employees;

    @NotNull(message = "Shift Rotation ID cannot be null.")
    private Long shiftRotationId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;
    
    private LocalDate expirationDate;
}
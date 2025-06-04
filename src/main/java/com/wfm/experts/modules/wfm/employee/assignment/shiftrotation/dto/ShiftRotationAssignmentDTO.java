package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRotationAssignmentDTO {
    private String employeeId;
    private Long shiftRotationId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
}

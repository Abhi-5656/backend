package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiShiftRotationAssignmentRequestDTO {
    private List<String> employees;
    private Long shiftRotationId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
}

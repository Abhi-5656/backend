package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyAssignmentDTO {

    private Long id;

    @NotEmpty(message = "Employee IDs list cannot be null or empty.")
    private List<String> employeeIds;

    @NotNull(message = "Pay Policy ID cannot be null.")
    private Long payPolicyId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private LocalDateTime assignedAt;

    private boolean active;
}
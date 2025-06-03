package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyAssignmentDTO {

    private Long id;

    private String employeeId;

    private Long payPolicyId;

    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private LocalDateTime assignedAt;

    private boolean active;

}

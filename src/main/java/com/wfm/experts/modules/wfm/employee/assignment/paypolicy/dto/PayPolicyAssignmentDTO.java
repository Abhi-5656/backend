package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto;

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

    // Changed from String to List<String> to support multiple employee IDs
    private List<String> employeeIds;

    private Long payPolicyId;

    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private LocalDateTime assignedAt;

    private boolean active;
}

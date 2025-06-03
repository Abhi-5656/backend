package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_policy_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "pay_policy_id", nullable = false)
    private Long payPolicyId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private boolean active;
}

package com.wfm.experts.modules.wfm.employee.assignment.requesttype.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_type_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTypeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "request_type_id", nullable = false)
    private Long requestTypeId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private boolean active;
}
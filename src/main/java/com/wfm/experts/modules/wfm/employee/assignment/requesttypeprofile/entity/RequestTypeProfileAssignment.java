package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_type_profile_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTypeProfileAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "request_type_profile_id", nullable = false)
    private Long requestTypeProfileId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private boolean active;
}
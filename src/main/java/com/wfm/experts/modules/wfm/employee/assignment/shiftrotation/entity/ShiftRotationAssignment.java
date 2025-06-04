package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity;

import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_shift_rotation_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_rotation_id", nullable = false)
    private ShiftRotation shiftRotation;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}

package com.wfm.experts.setup.wfm.shift.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shift_rotation_days")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to parent rotation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_rotation_id", nullable = false)
    private ShiftRotation shiftRotation;

    @Column(nullable = false)
    private Integer week; // week number (1-based)

    @Column(nullable = false, length = 10)
    private String weekday; // e.g. "Mon", "Tue", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
}

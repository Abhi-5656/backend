package com.wfm.experts.setup.wfm.shift.entity;

import com.wfm.experts.setup.wfm.shift.enums.Weekday;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shift_rotation_days")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotationDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_rotation_id", nullable = false)
    private ShiftRotation shiftRotation;

    @Column(nullable = false)
    private Integer week; // 1-based week number

//    @Column(nullable = false, length = 10)
//    private String weekday; // e.g. "Mon", "Tue", etc.
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Weekday weekday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift; // nullable if weekOff is true

    @Column(name = "week_off")
    private Boolean weekOff; // true for week off, null or false for working day
}

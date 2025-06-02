package com.wfm.experts.setup.wfm.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendance_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "full_day_hours")
    private Integer fullDayHours;     // e.g. 8

    @Column(name = "full_day_minutes")
    private Integer fullDayMinutes;   // e.g. 0

    @Column(name = "half_day_hours")
    private Integer halfDayHours;     // e.g. 4

    @Column(name = "half_day_minutes")
    private Integer halfDayMinutes;   // e.g. 0

    // Add auditing fields if needed (createdAt, updatedAt, etc)
}

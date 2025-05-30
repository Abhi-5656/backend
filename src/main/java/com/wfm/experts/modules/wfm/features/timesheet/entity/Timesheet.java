package com.wfm.experts.modules.wfm.features.timesheet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "timesheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // Total calculated work hours for this day (optional, for reporting)
    @Column(name = "total_work_duration")
    private Double totalWorkDuration; // in hours, or use Duration if you prefer

    // Optional: overtime, late/early flags, or other summary fields
    @Column(name = "overtime_duration")
    private Double overtimeDuration; // in hours

    @Column(name = "status", length = 32)
    private String status; // e.g., APPROVED, PENDING, EXCEPTION, etc.

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PunchEvent> punchEvents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

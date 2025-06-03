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

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // Total calculated work duration for this day (in minutes, integer)
    @Column(name = "work_duration_minutes")
    private Integer workDurationMinutes;

    // Optional: total work duration in hours (for analytics/reporting, e.g., 7.5 hours)
    @Column(name = "total_work_duration")
    private Double totalWorkDuration;

    // Optional: overtime duration in minutes
    @Column(name = "overtime_duration")
    private Integer overtimeDuration;

    // e.g., APPROVED, PENDING, EXCEPTION, etc.
    @Column(name = "status", length = 32)
    private String status;

    // Store PayPolicyRuleResultDTO as JSON for traceability/audit
    @Column(name = "rule_results_json", columnDefinition = "TEXT")
    private String ruleResultsJson;

    // When the timesheet was (re)calculated
    @Column(name = "calculated_at")
    private LocalDate calculatedAt;

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

package com.wfm.experts.modules.wfm.features.timesheet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "timesheets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_employee_work_date", columnNames = {"employee_id", "work_date"})
        })
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

    @Column(name = "regular_hours_minutes")
    private Integer regularHoursMinutes;

    @Column(name = "daily_ot_hours_minutes")
    private Integer dailyOtHoursMinutes;

    @Column(name = "excess_hours_minutes")
    private Integer excessHoursMinutes;

    @Column(name = "weekly_ot_hours_minutes")
    private Integer weeklyOtHoursMinutes;

    @Column(name = "total_work_duration_minutes")
    private Integer totalWorkDurationMinutes;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "rule_results_json", columnDefinition = "TEXT")
    private String ruleResultsJson;

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
package com.wfm.experts.modules.wfm.features.timesheet.entity;

import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchEventStatus;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*; // Ensure this is imported
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "punch_events"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false, length = 16)
    private PunchType punchType; // This field is now part of the composite unique key

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PunchEventStatus status;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "geo_lat")
    private Double geoLat;

    @Column(name = "geo_long")
    private Double geoLong;

    @Column(name = "notes", length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "exception_flag")
    private Boolean exceptionFlag;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
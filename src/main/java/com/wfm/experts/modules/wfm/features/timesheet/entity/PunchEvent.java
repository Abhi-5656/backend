package com.wfm.experts.modules.wfm.features.timesheet.entity;

import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchEventStatus;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "punch_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_employee_event_time", columnNames = {"employee_id", "event_time"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false, length = 16)
    private PunchType punchType;

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

    /**
     * Stores the employee's image as a Base64 encoded string.
     * The @Lob annotation ensures that a large text type column is used in the database.
     */
//    @Lob
//    @Column(name = "employee_image_base64")
//    private String employeeImageBase64;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", foreignKey = @ForeignKey(name = "fk_punch_events_timesheet"))
    private Timesheet timesheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", foreignKey = @ForeignKey(name = "fk_punch_events_shift"))
    private Shift shift;

    @Column(name = "exception_flag")
    private Boolean exceptionFlag = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = this.createdAt;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
public void setEventTime(LocalDateTime eventTime) {
    if (eventTime != null) {
        this.eventTime = ZonedDateTime.of(eventTime, ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .toLocalDateTime();
    } else {
        this.eventTime = null;
    }
}

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
    }
}

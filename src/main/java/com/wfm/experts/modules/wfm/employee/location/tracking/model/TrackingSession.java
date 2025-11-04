package com.wfm.experts.modules.wfm.employee.location.tracking.model;

import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.LineString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "tracking_session",
        indexes = {
                @Index(name = "idx_ts_emp_status", columnList = "employee_id,status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrackingStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "start_lat")
    private Double startLat;

    @Column(name = "start_lng")
    private Double startLng;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "last_seq")
    private Integer lastSeq;

    @Builder.Default
    @Column(name = "total_distance_m", nullable = false)
    private Double totalDistanceM = 0.0;

    @Builder.Default
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    // We store a 2D LineString here (the merge in SQL uses ST_Force2D on the M-geometry)
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "path_geometry", columnDefinition = "geometry(LineString,4326)")
    private LineString pathGeometry;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

package com.wfm.experts.modules.wfm.employee.location.tracking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_session")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(name = "clock_in_time", nullable = false)
    private OffsetDateTime clockInTime;

    @Column(name = "clock_out_time")
    private OffsetDateTime clockOutTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // OPEN / CLOSED

    @Column(name = "path_geometry", columnDefinition = "geometry(LineStringM,4326)")
    private LineString pathGeometry;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "total_distance_m")
    private Double totalDistanceM;

    @Column(name = "last_seq_processed")
    private Long lastSeqProcessed; // for idempotency across restarts
}

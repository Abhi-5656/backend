// com/wfm/experts/modules/wfm/employee/location/tracking/model/TrackingPoint.java
package com.wfm.experts.modules.wfm.employee.location.tracking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tracking_point",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_tp_session_seq", columnNames = {"session_id","seq"})
        },
        indexes = {
                @Index(name="idx_tp_session", columnList="session_id")
        })
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingPoint {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="session_id", nullable = false)
    private TrackingSession session;

    @Column(nullable = false) private Integer seq;
    @Column(nullable = false) private Double lat;
    @Column(nullable = false) private Double lng;
    @Column(name = "captured_at", nullable = false) private Instant capturedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

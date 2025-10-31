package com.wfm.experts.modules.wfm.employee.location.tracking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_path_chunk",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id","chunk_index"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingPathChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chunk_id")
    private Long chunkId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "point_count", nullable = false)
    private Integer pointCount;

    @Column(name = "chunk_geometry", nullable = false, columnDefinition = "geometry(LineStringM,4326)")
    private LineString chunkGeometry;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private OffsetDateTime endedAt;
}

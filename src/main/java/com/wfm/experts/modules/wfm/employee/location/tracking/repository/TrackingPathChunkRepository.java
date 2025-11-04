package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingPathChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TrackingPathChunkRepository extends JpaRepository<TrackingPathChunk, Long> {

    @Query(value = """
            SELECT COALESCE(MAX(c.chunk_index) + 1, 0)
            FROM tracking_path_chunk c
            WHERE c.session_id = :sessionId
            """, nativeQuery = true)
    Optional<Integer> nextChunkIndexForSession(@Param("sessionId") Long sessionId);

    @Query(value = """
            SELECT ST_AsGeoJSON(
                     ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                   )
            FROM tracking_path_chunk c
            WHERE c.session_id = :sessionId
            """, nativeQuery = true)
    String pathGeoJsonBySession(@Param("sessionId") Long sessionId);

    @Query(value = """
            SELECT ST_AsGeoJSON(
                     ST_Envelope(ST_Collect(c.chunk_geometry))
                   )
            FROM tracking_path_chunk c
            WHERE c.session_id = :sessionId
            """, nativeQuery = true)
    String bboxGeoJsonBySession(@Param("sessionId") Long sessionId);

    @Query(value = """
            SELECT
              c.chunk_index,
              c.point_count,
              c.started_at,
              c.ended_at,
              ST_AsGeoJSON(c.chunk_geometry) AS geojson
            FROM tracking_path_chunk c
            WHERE c.session_id = :sessionId
            ORDER BY c.chunk_index
            """, nativeQuery = true)
    List<Map<String, Object>> listChunks(@Param("sessionId") Long sessionId);
}

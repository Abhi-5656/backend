package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TrackingReadRepository {

    private final JdbcTemplate jdbc;

    /**
     * Single-row session summary with path/bbox GeoJSON aggregated from chunks.
     * Column names match your controller usage exactly.
     */
    public Map<String, Object> findSessionSummary(Long sessionId) {
        final String sql = """
            SELECT
                s.id                AS session_id,
                s.employee_id       AS employee_id,
                s.status            AS status,
                s.started_at        AS clock_in_time,
                s.ended_at          AS clock_out_time,
                COALESCE(s.total_points, 0)       AS total_points,
                COALESCE(s.total_distance_m, 0.0) AS total_distance_m,
                (
                    SELECT ST_AsGeoJSON(
                               ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                           )
                    FROM tracking_path_chunk c
                    WHERE c.session_id = s.id
                ) AS path_geojson,
                (
                    SELECT ST_AsGeoJSON(
                               ST_Envelope(ST_Collect(c.chunk_geometry))
                           )
                    FROM tracking_path_chunk c
                    WHERE c.session_id = s.id
                ) AS bbox_geojson
            FROM tracking_session s
            WHERE s.id = ?
            """;
        try {
            return jdbc.queryForMap(sql, sessionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * List of per-chunk rows with each chunk's GeoJSON.
     * Column aliases match your controller (chunk_index, point_count, started_at, ended_at, geojson).
     */
    public List<Map<String, Object>> listChunks(Long sessionId) {
        final String sql = """
            SELECT
                c.chunk_index,
                c.point_count,
                c.started_at,
                c.ended_at,
                ST_AsGeoJSON(c.chunk_geometry) AS geojson
            FROM tracking_path_chunk c
            WHERE c.session_id = ?
            ORDER BY c.chunk_index
            """;
        return jdbc.queryForList(sql, sessionId);
    }
}

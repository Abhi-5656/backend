package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackingQueryServiceImpl implements TrackingQueryService {

    private final TrackingSessionRepository sessionRepo;
    private final JdbcTemplate jdbc;

    @Override
    public LiveResponse getLive(String employeeId) {
        var session = sessionRepo.findFirstByEmployeeIdAndStatusOrderByClockInTimeDesc(employeeId, "OPEN")
                .orElseThrow(() -> new IllegalStateException("No OPEN session"));

        Map<String, Object> row = jdbc.queryForMap("""
            SELECT
              ST_AsGeoJSON(ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))) AS path_geojson,
              ST_Y(ST_EndPoint(ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index)))) AS lat,
              ST_X(ST_EndPoint(ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index)))) AS lng
            FROM tracking_path_chunk c
            WHERE c.session_id = ?
            """, session.getSessionId());

        return LiveResponse.builder()
                .sessionId(session.getSessionId())
                .polylineGeoJson((String) row.get("path_geojson"))
                .currentLat((Double) row.get("lat"))
                .currentLng((Double) row.get("lng"))
                .clockInTime(session.getClockInTime().toString())
                .totalPoints(session.getTotalPoints())
                .totalDistanceM(session.getTotalDistanceM())
                .build();
    }
}

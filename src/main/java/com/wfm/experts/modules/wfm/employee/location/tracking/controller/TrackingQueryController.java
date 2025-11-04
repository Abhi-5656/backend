// com/wfm/experts/modules/wfm/employee/location/tracking/controller/TrackingQueryController.java
package com.wfm.experts.modules.wfm.employee.location.tracking.controller;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ChunkResponse;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.SessionSummaryResponse;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPathChunkRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingQueryService;
import com.wfm.experts.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingQueryController {

    private final TrackingQueryService trackingQueryService;
    private final TrackingSessionRepository sessionRepo;
    private final TrackingPathChunkRepository chunkRepo;
    private final JwtUtil jwtUtil;

    private String empIdFromAuth(String auth) {
        if (auth == null) return null;
        return auth.startsWith("Bearer ") ? jwtUtil.extractEmployeeId(auth.substring(7)) : jwtUtil.extractEmployeeId(auth);
    }

    /** Live view (works for OPEN and CLOSED). */
    @GetMapping("/query/live/{sessionId}")
    public ResponseEntity<LiveResponse> live(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long sessionId) {

        String employeeId = empIdFromAuth(authorization);
        LiveResponse res = trackingQueryService.live(employeeId, sessionId);
        return ResponseEntity.ok(res);
    }

    /** Session summary from session header + merged chunks (no raw read repo). */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionSummaryResponse> getSession(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long sessionId) {

        String employeeId = empIdFromAuth(authorization);

        TrackingSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (!s.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String pathGeoJson = chunkRepo.pathGeoJsonBySession(sessionId);
        String bboxGeoJson = chunkRepo.bboxGeoJsonBySession(sessionId);

        SessionSummaryResponse dto = SessionSummaryResponse.builder()
                .sessionId(s.getId())
                .employeeId(s.getEmployeeId())
                .status(s.getStatus().name())
                .clockInTime(s.getStartedAt() != null ? s.getStartedAt().toString() : null)
                .clockOutTime(s.getEndedAt() != null ? s.getEndedAt().toString() : null)
                .totalPoints(s.getTotalPoints() != null ? s.getTotalPoints() : 0)
                .totalDistanceM(s.getTotalDistanceM() != null ? s.getTotalDistanceM() : 0.0)
                .pathGeoJson(pathGeoJson)
                .bboxGeoJson(bboxGeoJson)
                .build();

        return ResponseEntity.ok(dto);
    }

    /** Chunks list for a session (index, counts, window, geojson). */
    @GetMapping("/session/{sessionId}/chunks")
    public ResponseEntity<List<ChunkResponse>> getChunks(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long sessionId) {

        String employeeId = empIdFromAuth(authorization);

        TrackingSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Session not found: " + sessionId));

        if (!s.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Map<String, Object>> rows = chunkRepo.listChunks(sessionId);
        List<ChunkResponse> out = rows.stream().map(r -> ChunkResponse.builder()
                .chunkIndex(((Number) r.get("chunk_index")).intValue())
                .pointCount(((Number) r.get("point_count")).intValue())
                .startedAt(r.get("started_at") != null ? r.get("started_at").toString() : null)
                .endedAt(r.get("ended_at") != null ? r.get("ended_at").toString() : null)
                .geoJson((String) r.get("geojson"))
                .build()).toList();

        return ResponseEntity.ok(out);
    }
}

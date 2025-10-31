package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.entity.TrackingPathChunk;
import com.wfm.experts.modules.wfm.employee.location.tracking.entity.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPathChunkRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingChunkServiceImpl implements TrackingChunkService {

    private final TrackingSessionRepository sessionRepo;
    private final TrackingPathChunkRepository chunkRepo;
    private final JdbcTemplate jdbc;

    @Value("${tracking.chunk.size:10}")
    private int chunkSize;

    @Value("${tracking.chunk.maxWindowSeconds:60}")
    private int maxWindowSeconds;

    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

    private static class Agg {
        long lastSeq = -1;
        int nextChunkIdx = 0;
        OffsetDateTime lastFlushAt = OffsetDateTime.now();
        List<TrackingPointMessage> buffer = new ArrayList<>();
        boolean initialized = false;
    }
    private final Map<Long, Agg> aggregators = new HashMap<>();

    @Override
    @Transactional
    public void ingestPoint(TrackingPointMessage msg) {
        var session = sessionRepo.findById(msg.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown sessionId"));

        Agg a = aggregators.computeIfAbsent(msg.getSessionId(), k -> new Agg());

        // Initialize aggregator from DB if first time after restart
        if (!a.initialized) {
            long dbLastSeq = Optional.ofNullable(session.getLastSeqProcessed()).orElse(-1L);
            a.lastSeq = dbLastSeq;
            a.initialized = true;
        }

        // Idempotency / ordering
        if (msg.getSeq() <= a.lastSeq) {
            log.debug("Skip duplicate/old seq={} for session {}", msg.getSeq(), msg.getSessionId());
            return;
        }

        a.buffer.add(msg);
        a.lastSeq = msg.getSeq();

        boolean bySize = a.buffer.size() >= chunkSize;
        boolean byTime = OffsetDateTime.now().minusSeconds(maxWindowSeconds).isAfter(a.lastFlushAt);

        if (bySize || byTime) flush(msg.getSessionId(), a);
    }

    @Override
    @Transactional
    public void closeSession(TrackingCloseMessage msg) {
        Agg a = aggregators.get(msg.getSessionId());
        if (a != null && !a.buffer.isEmpty()) flush(msg.getSessionId(), a);

        // Merge chunks to header.path_geometry, compute distance, close
        jdbc.update("""
            UPDATE tracking_session s
               SET path_geometry = (
                     SELECT ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                       FROM tracking_path_chunk c
                      WHERE c.session_id = s.session_id
                   ),
                   clock_out_time = NOW(),
                   status = 'CLOSED',
                   total_distance_m = COALESCE(ST_Length(
                       (SELECT ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                          FROM tracking_path_chunk c
                         WHERE c.session_id = s.session_id
                       )::geography
                   ), 0),
                   last_seq_processed = GREATEST(COALESCE(last_seq_processed,-1), ?)
             WHERE s.session_id = ?
            """, (a != null ? a.lastSeq : msg.getSeq()), msg.getSessionId());

        aggregators.remove(msg.getSessionId());
    }

    private void flush(Long sessionId, Agg a) {
        if (a.buffer.isEmpty()) return;

        // build LineStringM (lon, lat, m=epochSeconds)
        Coordinate[] coords = new Coordinate[a.buffer.size()];
        for (int i = 0; i < a.buffer.size(); i++) {
            var p = a.buffer.get(i);
            double m = ZonedDateTime.parse(p.getCapturedAt()).toEpochSecond();
            coords[i] = new CoordinateXYM(p.getLng(), p.getLat(), m);
        }
        LineString ls = gf.createLineString(coords);

        OffsetDateTime started = OffsetDateTime.parse(a.buffer.get(0).getCapturedAt());
        OffsetDateTime ended   = OffsetDateTime.parse(a.buffer.get(a.buffer.size()-1).getCapturedAt());

        TrackingPathChunk chunk = TrackingPathChunk.builder()
                .sessionId(sessionId)
                .chunkIndex(a.nextChunkIdx)
                .pointCount(a.buffer.size())
                .chunkGeometry(ls)
                .startedAt(started)
                .endedAt(ended)
                .build();
        chunkRepo.save(chunk);

        // update totals + last_seq_processed
        jdbc.update("""
            UPDATE tracking_session s
               SET total_points = total_points + ?,
                   total_distance_m = total_distance_m + ST_Length(?::geography),
                   last_seq_processed = GREATEST(COALESCE(last_seq_processed,-1), ?)
             WHERE s.session_id = ?
            """, a.buffer.size(), wkt(ls), a.lastSeq, sessionId);

        a.buffer.clear();
        a.nextChunkIdx++;
        a.lastFlushAt = OffsetDateTime.now();
    }

    // SRID=4326;LINESTRINGM(lon lat m, ...)
    private String wkt(LineString ls) {
        StringBuilder sb = new StringBuilder("SRID=4326;LINESTRINGM(");
        for (int i = 0; i < ls.getNumPoints(); i++) {
            var c = ls.getCoordinateN(i);
            if (i > 0) sb.append(",");
            sb.append(c.getX()).append(" ").append(c.getY()).append(" ").append(c.getM());
        }
        sb.append(")");
        return sb.toString();
    }
}

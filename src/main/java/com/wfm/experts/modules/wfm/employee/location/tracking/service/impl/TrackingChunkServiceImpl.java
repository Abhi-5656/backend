package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingPathChunk;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPathChunkRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        Instant lastFlushAt = Instant.now();
        final List<TrackingPointMessage> buffer = new ArrayList<>();
        boolean initialized = false;
    }

    // sessionId -> in-memory aggregator
    private final Map<Long, Agg> aggregators = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void ingestPoint(TrackingPointMessage msg) {
        var session = sessionRepo.findById(msg.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown sessionId " + msg.getSessionId()));

        Agg a = aggregators.computeIfAbsent(msg.getSessionId(), k -> new Agg());

        synchronized (a) {
            if (!a.initialized) {
                long dbLastSeq = Optional.ofNullable(session.getLastSeq()).map(Integer::longValue).orElse(-1L);
                a.lastSeq = dbLastSeq;
                // repo should return Optional<Integer> for max(chunk_index)+1 (or 0 if none)
                a.nextChunkIdx = chunkRepo.nextChunkIndexForSession(session.getId()).orElse(0);
                a.initialized = true;
            }

            // idempotency / monotonic sequence
            if (msg.getSeq() <= a.lastSeq) {
                log.debug("Skip duplicate/old seq={} for session {}", msg.getSeq(), msg.getSessionId());
                return;
            }

            a.buffer.add(msg);
            a.lastSeq = msg.getSeq();

            boolean bySize = a.buffer.size() >= chunkSize;
            boolean byTime = Instant.now().minusSeconds(maxWindowSeconds).isAfter(a.lastFlushAt);

            if (bySize || byTime) {
                flush(msg.getSessionId(), a);
            }
        }
    }

    @Override
    @Transactional
    public void closeSession(TrackingCloseMessage msg) {
        Agg a = aggregators.get(msg.getSessionId());
        if (a != null) {
            synchronized (a) {
                if (!a.buffer.isEmpty()) flush(msg.getSessionId(), a);
            }
        }

        // Merge chunks into path_geometry (2D), compute distance, set CLOSED, update last_seq
        jdbc.update("""
            UPDATE tracking_session s
               SET path_geometry = ST_Force2D((
                     SELECT ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                       FROM tracking_path_chunk c
                      WHERE c.session_id = s.id
                   )),
                   ended_at = NOW(),
                   status = 'CLOSED',
                   total_distance_m = COALESCE(ST_Length((
                       SELECT ST_LineMerge(ST_Collect(c.chunk_geometry ORDER BY c.chunk_index))
                         FROM tracking_path_chunk c
                        WHERE c.session_id = s.id
                   )::geography), 0),
                   last_seq = GREATEST(COALESCE(last_seq,-1), ?)
             WHERE s.id = ?
            """, (a != null ? a.lastSeq : msg.getSeq()), msg.getSessionId());

        aggregators.remove(msg.getSessionId());
    }

    private void flush(Long sessionId, Agg a) {
        if (a.buffer.isEmpty()) return;

        // Build LineStringM: (lon, lat, m = epochSeconds)
        Coordinate[] coords = new Coordinate[a.buffer.size()];
        for (int i = 0; i < a.buffer.size(); i++) {
            var p = a.buffer.get(i);
            double m = p.getCapturedAt().getEpochSecond();
            coords[i] = new CoordinateXYM(p.getLng(), p.getLat(), m);
        }
        LineString ls = gf.createLineString(coords);

        // Convert Instant to OffsetDateTime (assume UTC offset)
        OffsetDateTime started = a.buffer.get(0).getCapturedAt().atOffset(ZoneOffset.UTC);
        OffsetDateTime ended = a.buffer.get(a.buffer.size() - 1).getCapturedAt().atOffset(ZoneOffset.UTC);

        // Create the TrackingPathChunk with OffsetDateTime
        TrackingPathChunk chunk = TrackingPathChunk.builder()
                .sessionId(sessionId)
                .chunkIndex(a.nextChunkIdx)
                .pointCount(a.buffer.size())
                .chunkGeometry(ls)
                .startedAt(started)
                .endedAt(ended)
                .build();
        chunkRepo.save(chunk);

        // Update totals + last_seq on header
        jdbc.update("""
        UPDATE tracking_session s
           SET total_points = total_points + ?,
               total_distance_m = total_distance_m + ST_Length(?::public.geography),
               last_seq = GREATEST(COALESCE(last_seq,-1), ?)
         WHERE s.id = ?
        """, a.buffer.size(), wkt(ls), a.lastSeq, sessionId);

        a.buffer.clear();
        a.nextChunkIdx++;
        a.lastFlushAt = Instant.now();
    }


    // SRID=4326;LINESTRINGM(lon lat m, ...)
    private static String wkt(LineString ls) {
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

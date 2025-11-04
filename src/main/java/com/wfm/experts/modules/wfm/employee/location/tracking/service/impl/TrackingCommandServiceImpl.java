//// com/wfm/experts/modules/wfm/employee/location/tracking/service/impl/TrackingCommandServiceImpl.java
//package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockInRequest;
//import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockOutRequest;
//import com.wfm.experts.modules.wfm.employee.location.tracking.dto.PointRequest;
//import com.wfm.experts.modules.wfm.employee.location.tracking.model.*;
//import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPointRepository;
//import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
//import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingCommandService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//
//@Service
//@RequiredArgsConstructor
//public class TrackingCommandServiceImpl implements TrackingCommandService {
//
//    private final TrackingSessionRepository sessionRepo;
//    private final TrackingPointRepository pointRepo;
//
//    @Override
//    @Transactional
//    public Long startSession(String employeeId, ClockInRequest req) {
//        // Idempotency: if an OPEN session exists, just return it
//        var existing = sessionRepo.findTopByEmployeeIdAndStatusOrderByIdDesc(employeeId, TrackingStatus.OPEN);
//        if (existing.isPresent()) return existing.get().getId();
//
//        var session = TrackingSession.builder()
//                .employeeId(employeeId)
//                .status(TrackingStatus.OPEN)
//                .startedAt(req.getCapturedAt() != null ? req.getCapturedAt() : Instant.now())
//                .startLat(req.getLat())
//                .startLng(req.getLng())
//                .lastLat(req.getLat())
//                .lastLng(req.getLng())
//                .lastSeq(0)
//                .build();
//        session = sessionRepo.save(session);
//
//        // OPTIONAL: publish "session-opened" event to Rabbit here (tenant header from TenantContext)
//        return session.getId();
//    }
//
//    @Override
//    @Transactional
//    public void addPoint(String employeeId, PointRequest req) {
//        var session = sessionRepo.findById(req.getSessionId())
//                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
//
//        if (!employeeId.equals(session.getEmployeeId())) {
//            throw new IllegalArgumentException("Session does not belong to employee");
//        }
//        if (session.getStatus() != TrackingStatus.OPEN) {
//            throw new IllegalStateException("Session is not OPEN");
//        }
//
//        // Idempotency: unique (sessionId, seq) index protects us; ignore duplicates
//        try {
//            var p = TrackingPoint.builder()
//                    .session(session)
//                    .seq(req.getSeq())
//                    .lat(req.getLat())
//                    .lng(req.getLng())
//                    .capturedAt(req.getCapturedAt() != null ? req.getCapturedAt() : Instant.now())
//                    .build();
//            pointRepo.save(p);
//
//            // maintain "last* / lastSeq"
//            if (session.getLastSeq() == null || req.getSeq() > session.getLastSeq()) {
//                session.setLastSeq(req.getSeq());
//                session.setLastLat(req.getLat());
//                session.setLastLng(req.getLng());
//                sessionRepo.save(session);
//            }
//        } catch (DataIntegrityViolationException dup) {
//            // duplicate seq — treat as success (idempotent write)
//        }
//
//        // OPTIONAL: publish "tracking-point" event to Rabbit here (with X-Tenant-Id header)
//    }
//
//    @Override
//    @Transactional
//    public void closeSession(String employeeId, ClockOutRequest req) {
//        var session = sessionRepo.findById(req.getSessionId())
//                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
//
//        if (!employeeId.equals(session.getEmployeeId())) {
//            throw new IllegalArgumentException("Session does not belong to employee");
//        }
//        if (session.getStatus() != TrackingStatus.OPEN) {
//            return; // already closed; idempotent
//        }
//
//        session.setStatus(TrackingStatus.CLOSED);
//        session.setEndedAt(Instant.now());
//        if (req.getSeq() != null && (session.getLastSeq() == null || req.getSeq() > session.getLastSeq())) {
//            session.setLastSeq(req.getSeq());
//        }
//        sessionRepo.save(session);
//
//        // OPTIONAL: publish "session-closed" event
//    }
//}

package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockInRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockOutRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.PointRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingStatus;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingChunkService;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingCommandService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrackingCommandServiceImpl implements TrackingCommandService {

    private final TrackingSessionRepository sessionRepo;
    private final TrackingChunkService chunkService;

    @Override
    @Transactional
    public Long startSession(String employeeId, ClockInRequest req) {
        Instant now = Instant.now();

        TrackingSession s = TrackingSession.builder()
                .employeeId(employeeId)
                .status(TrackingStatus.OPEN)
                .startedAt(now)
                .startLat(req.getLat())
                .startLng(req.getLng())
                .lastLat(req.getLat())
                .lastLng(req.getLng())
                .lastSeq(0) // first point will be seq=0
                .build();

        s = sessionRepo.saveAndFlush(s);

        TrackingPointMessage first = TrackingPointMessage.builder()
                .sessionId(s.getId())
                .employeeId(employeeId)
                .seq(0L)
                .lat(req.getLat())
                .lng(req.getLng())
                .capturedAt(req.getCapturedAt())
                .build();

        chunkService.ingestPoint(first);
        return s.getId();
    }

    @Override
    @Transactional
    public void addPoint(String employeeId, PointRequest req) {
        TrackingSession s = sessionRepo.findById(req.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + req.getSessionId()));

        if (!Objects.equals(s.getEmployeeId(), employeeId)) {
            throw new IllegalStateException("Session does not belong to employee");
        }
        if (s.getStatus() != TrackingStatus.OPEN) {
            throw new IllegalStateException("Session is not OPEN");
        }

        // Keep header fresh for live views / quick checks
        s.setLastLat(req.getLat());
        s.setLastLng(req.getLng());
        Integer prev = s.getLastSeq() == null ? 0 : s.getLastSeq();
        if (req.getSeq() != null && req.getSeq() > prev) {
            s.setLastSeq(req.getSeq());
        }
        sessionRepo.save(s);

        TrackingPointMessage msg = TrackingPointMessage.builder()
                .sessionId(req.getSessionId())
                .employeeId(employeeId)
                .seq(req.getSeq().longValue())
                .lat(req.getLat())
                .lng(req.getLng())
                .capturedAt(req.getCapturedAt())
                .build();

        chunkService.ingestPoint(msg);
    }

    @Override
    @Transactional
    public void closeSession(String employeeId, ClockOutRequest req) {
        TrackingSession s = sessionRepo.findById(req.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + req.getSessionId()));

        if (!Objects.equals(s.getEmployeeId(), employeeId)) {
            throw new IllegalStateException("Session does not belong to employee");
        }
        if (s.getStatus() != TrackingStatus.OPEN) {
            // idempotent close
            return;
        }

        TrackingCloseMessage msg = TrackingCloseMessage.builder()
                .sessionId(req.getSessionId())
                .employeeId(employeeId)
                .seq(req.getSeq().longValue())
                .build();

        chunkService.closeSession(msg);
    }
}


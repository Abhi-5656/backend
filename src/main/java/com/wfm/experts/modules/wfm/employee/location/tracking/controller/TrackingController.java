package com.wfm.experts.modules.wfm.employee.location.tracking.controller;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.*;
import com.wfm.experts.modules.wfm.employee.location.tracking.entity.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.producer.TrackingProducer;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final JwtUtil jwtUtil;
    private final TrackingSessionRepository sessionRepo;
    private final TrackingProducer producer;

    @PostMapping("/clock-in")
    @Transactional
    public ResponseEntity<ClockInResponse> clockIn(@RequestHeader("Authorization") String auth,
                                                   @Valid @RequestBody ClockInRequest req,
                                                   @RequestHeader(value="X-Tenant-ID", required=false) String tenantId) {
        String employeeId =jwtUtil.extractEmployeeId(auth.substring("Bearer ".length()));
//        String employeeId = jwtUtil.getUsernameFromToken(auth.substring("Bearer ".length()));

        // Create OPEN session
        TrackingSession s = TrackingSession.builder()
                .employeeId(employeeId)
                .clockInTime(OffsetDateTime.now())
                .status("OPEN")
                .totalPoints(0)
                .totalDistanceM(0.0)
                .lastSeqProcessed(-1L)
                .build();
        s = sessionRepo.save(s);

        // Publish first point; frontend may send seq=0 in next call too, both are OK due to idempotency
        producer.publishPoint(TrackingPointMessage.builder()
                .sessionId(s.getSessionId())
                .employeeId(employeeId)
                .seq(0L)
                .lat(req.getLat())
                .lng(req.getLng())
                .capturedAt(req.getCapturedAt())
                .tenantId(tenantId)
                .build());

        return ResponseEntity.ok(ClockInResponse.builder()
                .sessionId(s.getSessionId())
                .status("OPEN")
                .build());
    }

    @PostMapping("/point")
    public ResponseEntity<?> point(@RequestHeader("Authorization") String auth,
                                   @Valid @RequestBody PointRequest req,
                                   @RequestHeader(value="X-Tenant-ID", required=false) String tenantId) {
        String employeeId =jwtUtil.extractEmployeeId(auth.substring("Bearer ".length()));
        producer.publishPoint(TrackingPointMessage.builder()
                .sessionId(req.getSessionId())
                .employeeId(employeeId)
                .seq(req.getSeq())
                .lat(req.getLat())
                .lng(req.getLng())
                .capturedAt(req.getCapturedAt())
                .tenantId(tenantId)
                .build());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestHeader("Authorization") String auth,
                                      @Valid @RequestBody CloseRequest req,
                                      @RequestHeader(value="X-Tenant-ID", required=false) String tenantId) {
        String employeeId =jwtUtil.extractEmployeeId(auth.substring("Bearer ".length()));
        producer.publishClose(TrackingCloseMessage.builder()
                .sessionId(req.getSessionId())
                .employeeId(employeeId)
                .seq(req.getSeq())
                .tenantId(tenantId)
                .build());
        return ResponseEntity.accepted().build();
    }
}

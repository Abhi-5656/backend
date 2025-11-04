// com/wfm/experts/modules/wfm/employee/location/tracking/controller/TrackingCommandController.java
package com.wfm.experts.modules.wfm.employee.location.tracking.controller;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockInRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockOutRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.PointRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingCommandService;
import com.wfm.experts.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingCommandController {

    private final TrackingCommandService service;
    private final JwtUtil jwtUtil;

    private String empIdFromAuth(String auth) {
        if (auth == null) return null;
        return auth.startsWith("Bearer ") ? jwtUtil.extractEmployeeId(auth.substring(7)) : jwtUtil.extractEmployeeId(auth);
    }

    @PostMapping("/clock-in")
    public ResponseEntity<Map<String, Object>> clockIn(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody ClockInRequest req) {
        String employeeId = empIdFromAuth(auth);
        Long sessionId = service.startSession(employeeId, req);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "status", "OPEN"));
    }

    @PostMapping("/point")
    public ResponseEntity<Void> point(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody PointRequest req) {
        String employeeId = empIdFromAuth(auth);
        service.addPoint(employeeId, req);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/clock-out")
    public ResponseEntity<Map<String, Object>> clockOut(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody ClockOutRequest req) {
        String employeeId = empIdFromAuth(auth);
        service.closeSession(employeeId, req);
        return ResponseEntity.ok(Map.of("status", "CLOSED"));
    }
}

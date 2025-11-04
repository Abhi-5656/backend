//// com/wfm/experts/modules/wfm/employee/location/tracking/service/impl/TrackingQueryServiceImpl.java
//package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;
//import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingPoint;
//import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingStatus;
//import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPointRepository;
//
//import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
//import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingQueryService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TrackingQueryServiceImpl implements TrackingQueryService {
//
//    private final TrackingSessionRepository sessionRepo;
//    private final TrackingPointRepository pointRepo;
//
//    @Override
//    public LiveResponse getLive(String employeeId) {
//        var session = sessionRepo.findTopByEmployeeIdAndStatusOrderByIdDesc(employeeId, TrackingStatus.OPEN)
//                .orElseThrow(() -> new EntityNotFoundException("No OPEN session"));
//
//        List<TrackingPoint> pts = pointRepo.findBySessionOrderBySeqAsc(session);
//
//        int totalPoints = pts.size();
//        double distanceM = 0d;
//        for (int i = 1; i < pts.size(); i++) {
//            distanceM += haversineM(pts.get(i-1).getLat(), pts.get(i-1).getLng(),
//                    pts.get(i).getLat(), pts.get(i).getLng());
//        }
//
//        return LiveResponse.builder()
//                .sessionId(session.getId())
//                .currentLat(session.getLastLat())
//                .currentLng(session.getLastLng())
//                .clockInTime(session.getStartedAt())
//                .totalPoints(totalPoints)
//                .totalDistanceM(distanceM)
//                .polylineGeoJson(null) // fill if you have polyline builder
//                .build();
//    }
//
//    // simple haversine in meters
//    private static double haversineM(double lat1, double lon1, double lat2, double lon2) {
//        double R = 6371000d; // m
//        double dLat = Math.toRadians(lat2-lat1);
//        double dLon = Math.toRadians(lon2-lon1);
//        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                        Math.sin(dLon/2) * Math.sin(dLon/2);
//        return 2 * R * Math.asin(Math.sqrt(a));
//    }
//}



package com.wfm.experts.modules.wfm.employee.location.tracking.service.impl;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingPathChunkRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import com.wfm.experts.modules.wfm.employee.location.tracking.service.TrackingQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingQueryServiceImpl implements TrackingQueryService {

    private final TrackingSessionRepository sessionRepo;
    private final TrackingPathChunkRepository chunkRepo;

    @Override
    public LiveResponse live(String employeeId, Long sessionId) {
        TrackingSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (!s.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Session not owned by employee");
        }

        String pathGeoJson = chunkRepo.pathGeoJsonBySession(sessionId);

        return LiveResponse.builder()
                .sessionId(sessionId)
                .currentLat(s.getLastLat())
                .currentLng(s.getLastLng())
                .clockInTime(s.getStartedAt())
                .totalPoints(s.getTotalPoints() == null ? 0 : s.getTotalPoints())
                .totalDistanceM(s.getTotalDistanceM() == null ? 0.0 : s.getTotalDistanceM())
                .polylineGeoJson(pathGeoJson)
                .build();
    }
}

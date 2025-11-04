package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingSession;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {
    Optional<TrackingSession> findTopByEmployeeIdAndStatusOrderByIdDesc(String employeeId, TrackingStatus status);
}
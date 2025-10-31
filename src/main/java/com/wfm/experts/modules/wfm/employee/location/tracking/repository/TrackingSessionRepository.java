package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import com.wfm.experts.modules.wfm.employee.location.tracking.entity.TrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {
    Optional<TrackingSession> findFirstByEmployeeIdAndStatusOrderByClockInTimeDesc(String employeeId, String status);


}

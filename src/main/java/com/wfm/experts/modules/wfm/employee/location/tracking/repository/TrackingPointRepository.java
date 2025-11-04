// com/wfm/experts/modules/wfm/employee/location/tracking/repo/TrackingPointRepository.java
package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingPoint;
import com.wfm.experts.modules.wfm.employee.location.tracking.model.TrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingPointRepository extends JpaRepository<TrackingPoint, Long> {
    long countBySession(TrackingSession session);
    List<TrackingPoint> findBySessionOrderBySeqAsc(TrackingSession session);
}

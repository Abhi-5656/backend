package com.wfm.experts.modules.wfm.employee.location.tracking.repository;

import com.wfm.experts.modules.wfm.employee.location.tracking.entity.TrackingPathChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingPathChunkRepository extends JpaRepository<TrackingPathChunk, Long> {

}

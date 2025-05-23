package com.wfm.experts.setup.wfm.shift.repository;

import com.wfm.experts.setup.wfm.shift.entity.ShiftRotationDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftRotationDayRepository extends JpaRepository<ShiftRotationDay, Long> {
    List<ShiftRotationDay> findByShiftRotationId(Long shiftRotationId);
    List<ShiftRotationDay> findByShiftRotationIdAndWeek(Long shiftRotationId, Integer week);
}

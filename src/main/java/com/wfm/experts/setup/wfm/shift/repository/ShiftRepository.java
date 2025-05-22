package com.wfm.experts.setup.wfm.shift.repository;

import com.wfm.experts.setup.wfm.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByShiftName(String shiftName);
    boolean existsByShiftName(String shiftName);
}

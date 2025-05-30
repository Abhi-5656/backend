package com.wfm.experts.setup.wfm.shift.repository;

import com.wfm.experts.setup.wfm.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByShiftName(String shiftName);
    boolean existsByShiftName(String shiftName);

    // In ShiftRepository:
    @Query("SELECT s FROM Shift s WHERE s.isActive = true AND (s.calendarDate = :date OR s.calendarDate IS NULL)")
    List<Shift> findAllActiveShiftsForDate(@Param("date") LocalDate date);

}

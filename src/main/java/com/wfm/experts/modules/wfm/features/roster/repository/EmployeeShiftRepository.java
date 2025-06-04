package com.wfm.experts.modules.wfm.features.roster.repository;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    List<EmployeeShift> findByEmployeeId(String employeeId);

    // Only fetch non-deleted shifts in date range
    List<EmployeeShift> findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(
            String employeeId, LocalDate startDate, LocalDate endDate
    );

    // For backwards compatibility if you ever need it:
    List<EmployeeShift> findByEmployeeIdAndCalendarDateBetween(
            String employeeId, LocalDate startDate, LocalDate endDate
    );
}

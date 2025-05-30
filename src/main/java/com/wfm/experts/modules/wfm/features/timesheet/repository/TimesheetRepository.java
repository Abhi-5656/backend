package com.wfm.experts.modules.wfm.features.timesheet.repository;

import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    // Find a timesheet for an employee and a specific work date
    Optional<Timesheet> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    // Find all timesheets for an employee in a date range
    List<Timesheet> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate start, LocalDate end);
}

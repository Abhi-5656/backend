package com.wfm.experts.modules.wfm.features.timesheet.repository;

import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    // Preferred: For new code, always use String employeeId
    Optional<Timesheet> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);

    List<Timesheet> findByEmployeeIdAndWorkDateBetween(String employeeId, LocalDate start, LocalDate end);


}

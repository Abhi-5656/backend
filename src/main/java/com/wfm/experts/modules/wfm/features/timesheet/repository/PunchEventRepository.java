package com.wfm.experts.modules.wfm.features.timesheet.repository;

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PunchEventRepository extends JpaRepository<PunchEvent, Long> {
    // Find all punch events for an employee on a specific date (if needed)
    List<PunchEvent> findByEmployeeIdAndEventTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end);

    // Find by timesheet
    List<PunchEvent> findByTimesheetId(Long timesheetId);

    // Additional custom queries as needed
}

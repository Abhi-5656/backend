package com.wfm.experts.modules.wfm.features.roster.repository;

import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterProjection;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    List<EmployeeShift> findByEmployeeId(String employeeId);

    // Only fetch non-deleted shifts in date range
//    List<EmployeeShift> findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(
//            String employeeId, LocalDate startDate, LocalDate endDate
//    );

    // Fetch shifts for multiple employees in a date range, not deleted
    List<EmployeeShift> findByEmployeeIdInAndCalendarDateBetweenAndDeletedFalse(
            List<String> employeeIds, LocalDate startDate, LocalDate endDate
    );


    // For backwards compatibility if you ever need it:
    List<EmployeeShift> findByEmployeeIdAndCalendarDateBetween(
            String employeeId, LocalDate startDate, LocalDate endDate
    );


    @Query(value = """
WITH date_range AS (
  SELECT generate_series(:startDate, :endDate, interval '1 day')::date AS calendar_date
)
SELECT
  e.employee_id AS employeeId,
  pi.full_name AS fullName,
  d.calendar_date AS calendarDate,
  sh.id AS shiftId,
  sh.shift_name AS shiftName,
  sh.start_time AS shiftStartTime,
  sh.end_time AS shiftEndTime,
  sh.color AS shiftColor,
  sh.shift_label AS shiftTag,
  s.is_week_off AS isWeekOff,
  s.is_holiday AS isHoliday,
  s.weekday AS weekday,
  s.deleted AS deleted,            -- <--- Added
  s.assigned_by AS assignedBy      -- <--- Added
FROM employees e
LEFT JOIN employee_personal_info pi
  ON e.personal_info_id = pi.id
CROSS JOIN date_range d
LEFT JOIN employee_shifts s
  ON s.employee_id = e.employee_id AND s.calendar_date = d.calendar_date
LEFT JOIN shifts sh
  ON s.shift_id = sh.id
ORDER BY e.employee_id, d.calendar_date
""", nativeQuery = true)
    List<EmployeeShiftRosterProjection> findEmployeeShiftRosterWithShiftDetails(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



}

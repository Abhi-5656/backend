package com.wfm.experts.modules.wfm.features.roster.repository;

import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterProjection;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    List<EmployeeShift> findByEmployeeId(String employeeId);

    // Only fetch non-deleted shifts in date range
//    List<EmployeeShift> findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(
//            String employeeId, LocalDate startDate, LocalDate endDate
//    );
    /**
     * Finds the active shift for a given employee on a specific calendar date.
     * This is an alias for findByEmployeeIdAndCalendarDateAndDeletedFalse to resolve the error.
     * @param employeeId The ID of the employee.
     * @param calendarDate The specific date.
     * @return An Optional containing the EmployeeShift if an active one is found.
     */
    @Query("SELECT s FROM EmployeeShift s WHERE s.employeeId = :employeeId AND s.calendarDate = :calendarDate AND s.deleted = false")
    Optional<EmployeeShift> findByEmployeeIdAndCalendarDate(@Param("employeeId") String employeeId, @Param("calendarDate") LocalDate calendarDate);

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

//
//    @Modifying
//    @Query("UPDATE EmployeeShift es SET es.shift = :shift, es.deleted = false, es.assignedBy = :assignedBy " +
//            "WHERE es.employeeId IN :employeeIds AND es.calendarDate IN :calendarDates")
//    int bulkUpdateEmployeeShifts(
//            @Param("shift") Shift shift,
//            @Param("employeeIds") List<String> employeeIds,
//            @Param("calendarDates") List<LocalDate> calendarDates,
//            @Param("assignedBy") String assignedBy
//    );




}

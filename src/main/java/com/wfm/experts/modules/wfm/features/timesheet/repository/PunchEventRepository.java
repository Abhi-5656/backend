package com.wfm.experts.modules.wfm.features.timesheet.repository;

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PunchEventRepository extends JpaRepository<PunchEvent, Long> {
    // All punch events for an employee within a day
    List<PunchEvent> findByEmployeeIdAndEventTimeBetween(String employeeId, LocalDateTime start, LocalDateTime end);

    // All punch events for a given timesheet
    List<PunchEvent> findByTimesheetId(Long timesheetId);

    // All punch events for an employee (all-time, audit)
    List<PunchEvent> findByEmployeeId(String employeeId);

    // Latest punch event for an employee (e.g., for live dashboards, alerting)
    Optional<PunchEvent> findFirstByEmployeeIdOrderByEventTimeDesc(String employeeId);

    // Find all punch events for a set of employees within a given time period
    List<PunchEvent> findByEmployeeIdInAndEventTimeBetween(List<String> employeeIds, LocalDateTime start, LocalDateTime end);

    // Find missed/exception punches (assuming you have a PunchEventStatus or exception flag)
    List<PunchEvent> findByEmployeeIdAndExceptionFlagTrueAndEventTimeBetween(
            String employeeId, LocalDateTime start, LocalDateTime end);

    Optional<PunchEvent> findFirstByEmployeeIdAndPunchTypeAndEventTimeBeforeOrderByEventTimeDesc(
            String employeeId, PunchType punchType, LocalDateTime eventTime);


    // Get IN/OUT punches specifically (assuming you have a punchType field)
    @Query("SELECT p FROM PunchEvent p WHERE p.employeeId = :employeeId AND p.punchType = :punchType AND p.eventTime BETWEEN :start AND :end")
    List<PunchEvent> findByEmployeeIdAndPunchTypeAndEventTimeBetween(
            @Param("employeeId") String employeeId,
            @Param("punchType") String punchType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find all punches for a given shift (if you have shiftId on PunchEvent)
    List<PunchEvent> findByShiftId(Long shiftId);

    // All punches in a given period (audit, system-wide queries)
    List<PunchEvent> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);

    // Find duplicate punches (within a short interval, e.g., for anti-spoof or accidental double punch detection)
    @Query("SELECT p FROM PunchEvent p WHERE p.employeeId = :employeeId AND p.eventTime BETWEEN :start AND :end GROUP BY p.eventTime HAVING COUNT(p) > 1")
    List<PunchEvent> findDuplicatePunches(
            @Param("employeeId") String employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find earliest punch for an employee on a given day (first IN punch)
    Optional<PunchEvent> findFirstByEmployeeIdAndEventTimeBetweenOrderByEventTimeAsc(
            String employeeId, LocalDateTime start, LocalDateTime end);

    // Find latest punch for an employee on a given day (last OUT punch)
    Optional<PunchEvent> findFirstByEmployeeIdAndEventTimeBetweenOrderByEventTimeDesc(
            String employeeId, LocalDateTime start, LocalDateTime end);

    // Same as above, but using 'All' for clarity and batch processing
    List<PunchEvent> findAllByEmployeeIdAndEventTimeBetween(
            String employeeId,
            LocalDateTime start,
            LocalDateTime end
    );
}

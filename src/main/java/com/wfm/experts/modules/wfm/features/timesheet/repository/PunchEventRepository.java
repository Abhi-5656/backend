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
    List<PunchEvent> findByEmployeeIdAndEventTimeBetween(String employeeId, LocalDateTime start, LocalDateTime end);

    List<PunchEvent> findByTimesheetId(Long timesheetId);

    List<PunchEvent> findByEmployeeId(String employeeId);

    Optional<PunchEvent> findFirstByEmployeeIdOrderByEventTimeDesc(String employeeId);

    List<PunchEvent> findByEmployeeIdInAndEventTimeBetween(List<String> employeeIds, LocalDateTime start, LocalDateTime end);

    List<PunchEvent> findByEmployeeIdAndExceptionFlagTrueAndEventTimeBetween(
            String employeeId, LocalDateTime start, LocalDateTime end);

    Optional<PunchEvent> findFirstByEmployeeIdAndPunchTypeAndEventTimeBeforeOrderByEventTimeDesc(
            String employeeId, PunchType punchType, LocalDateTime eventTime);


    @Query("SELECT p FROM PunchEvent p WHERE p.employeeId = :employeeId AND p.punchType = :punchType AND p.eventTime BETWEEN :start AND :end")
    List<PunchEvent> findByEmployeeIdAndPunchTypeAndEventTimeBetween(
            @Param("employeeId") String employeeId,
            @Param("punchType") String punchType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<PunchEvent> findByShiftId(Long shiftId);

    List<PunchEvent> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM PunchEvent p WHERE p.employeeId = :employeeId AND p.eventTime BETWEEN :start AND :end GROUP BY p.eventTime HAVING COUNT(p) > 1")
    List<PunchEvent> findDuplicatePunches(
            @Param("employeeId") String employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Optional<PunchEvent> findFirstByEmployeeIdAndEventTimeBetweenOrderByEventTimeAsc(
            String employeeId, LocalDateTime start, LocalDateTime end);

    Optional<PunchEvent> findFirstByEmployeeIdAndEventTimeBetweenOrderByEventTimeDesc(
            String employeeId, LocalDateTime start, LocalDateTime end);

    List<PunchEvent> findAllByEmployeeIdAndEventTimeBetween(
            String employeeId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByEmployeeIdAndEventTimeBetween(String employeeId, LocalDateTime start, LocalDateTime end);
}
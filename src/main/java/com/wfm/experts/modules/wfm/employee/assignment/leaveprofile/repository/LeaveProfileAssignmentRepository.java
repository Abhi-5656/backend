package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveProfileAssignmentRepository extends JpaRepository<LeaveProfileAssignment, Long> {

    List<LeaveProfileAssignment> findByEmployeeId(String employeeId);

    /**
     * Finds the single active assignment for an employee on a given date.
     * This query is crucial for the "supersede" logic.
     */
    @Query("SELECT a FROM LeaveProfileAssignment a WHERE a.employeeId = :employeeId " +
            "AND a.effectiveDate <= :targetDate " +
            "AND (a.expirationDate IS NULL OR a.expirationDate >= :targetDate) " +
            "AND a.active = true")
    Optional<LeaveProfileAssignment> findActiveAssignmentOnDate(
            @Param("employeeId") String employeeId,
            @Param("targetDate") LocalDate targetDate);

    /**
     * Checks if any assignment exists for the employee that overlaps with the given date range.
     * An overlap occurs if (StartDate1 <= EndDate2) and (EndDate1 >= StartDate2).
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM LeaveProfileAssignment a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.effectiveDate <= :endDate " +
            "AND (a.expirationDate IS NULL OR a.expirationDate >= :startDate)")
    boolean existsOverlappingAssignment(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
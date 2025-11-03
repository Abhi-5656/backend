package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity.HolidayProfileAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayProfileAssignmentRepository extends JpaRepository<HolidayProfileAssignment, Long> {

    /**
     * Retrieve all holiday profile assignments for a given employee ID.
     */
    List<HolidayProfileAssignment> findByEmployeeId(String employeeId);

    /**
     * Checks if any assignment exists for the employee that overlaps with the given date range.
     * An overlap occurs if (StartDate1 <= EndDate2) and (EndDate1 >= StartDate2).
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
            "FROM HolidayProfileAssignment h " +
            "WHERE h.employeeId = :employeeId " +
            "AND h.effectiveDate <= :endDate " +
            "AND (h.expirationDate IS NULL OR h.expirationDate >= :startDate)")
    boolean existsOverlappingAssignment(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
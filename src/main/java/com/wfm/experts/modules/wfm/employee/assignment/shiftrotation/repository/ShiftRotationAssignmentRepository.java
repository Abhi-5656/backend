package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.repository;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRotationAssignmentRepository extends JpaRepository<ShiftRotationAssignment, Long> {

    @Query("""
    SELECT a FROM ShiftRotationAssignment a
    WHERE a.employeeId = :employeeId
      AND a.effectiveDate <= :date
      AND (a.expirationDate IS NULL OR :date <= a.expirationDate)
""")
    List<ShiftRotationAssignment> findByEmployeeIdAndDate(String employeeId, LocalDate date);


    @Query("""
        SELECT a FROM ShiftRotationAssignment a
        WHERE a.employeeId = :employeeId
          AND a.effectiveDate <= :endDate
          AND (a.expirationDate IS NULL OR a.expirationDate >= :startDate)
    """)
    List<ShiftRotationAssignment> findAllByEmployeeIdAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}

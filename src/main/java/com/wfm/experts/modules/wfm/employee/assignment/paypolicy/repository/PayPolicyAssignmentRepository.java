package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayPolicyAssignmentRepository extends JpaRepository<PayPolicyAssignment, Long> {
    List<PayPolicyAssignment> findByEmployeeId(String employeeId);

    // Find assignment active on a specific date (inclusive)
    Optional<PayPolicyAssignment> findByEmployeeIdAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqual(
            String employeeId, LocalDate date1, LocalDate date2);

    // Find ALL assignments for an employee that overlap a given date range
    @Query("SELECT p FROM PayPolicyAssignment p WHERE p.employeeId = :employeeId " +
            "AND p.effectiveDate <= :endDate AND (p.expirationDate IS NULL OR p.expirationDate >= :startDate)")
    List<PayPolicyAssignment> findAllOverlappingAssignments(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find the current/active assignment for an employee on a specific date
    @Query("SELECT p FROM PayPolicyAssignment p WHERE p.employeeId = :employeeId " +
            "AND p.effectiveDate <= :targetDate " +
            "AND (p.expirationDate IS NULL OR p.expirationDate >= :targetDate)")
    Optional<PayPolicyAssignment> findActiveAssignment(
            @Param("employeeId") String employeeId,
            @Param("targetDate") LocalDate targetDate);

    // Find all assignments for a list of employees on a specific date
    @Query("SELECT p FROM PayPolicyAssignment p WHERE p.employeeId IN :employeeIds " +
            "AND p.effectiveDate <= :targetDate " +
            "AND (p.expirationDate IS NULL OR p.expirationDate >= :targetDate)")
    List<PayPolicyAssignment> findActiveAssignmentsForEmployees(
            @Param("employeeIds") List<String> employeeIds,
            @Param("targetDate") LocalDate targetDate);

    // Find all assignments that are currently active (useful for audits)
    @Query("SELECT p FROM PayPolicyAssignment p WHERE p.effectiveDate <= CURRENT_DATE " +
            "AND (p.expirationDate IS NULL OR p.expirationDate >= CURRENT_DATE)")
    List<PayPolicyAssignment> findAllCurrentlyActiveAssignments();

    // Overlap check: Find if there is any assignment overlapping the given date range for this employee
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PayPolicyAssignment p " +
            "WHERE p.employeeId = :employeeId " +
            "AND p.effectiveDate <= :endDate AND (p.expirationDate IS NULL OR p.expirationDate >= :startDate)")
    boolean existsOverlappingAssignment(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get all assignments expiring on or before a date (for housekeeping)
    List<PayPolicyAssignment> findByExpirationDateLessThanEqual(LocalDate expirationDate);

    // Find all assignments created after a certain date (for change tracking)
    List<PayPolicyAssignment> findByEffectiveDateGreaterThanEqual(LocalDate effectiveDate);
}

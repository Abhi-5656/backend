package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // <-- IMPORT
import java.util.List;

@Repository
public interface LeaveBalanceLedgerRepository extends JpaRepository<LeaveBalanceLedger, Long> {

    /**
     * Finds all transactions for a specific employee.
     */
    List<LeaveBalanceLedger> findByEmployee_EmployeeId(String employeeId);

    /**
     * Finds all transactions for a specific employee and policy.
     */
    List<LeaveBalanceLedger> findByEmployee_EmployeeIdAndLeavePolicy_Id(String employeeId, Long leavePolicyId);

    /**
     * Calculates the current net balance by summing all transaction amounts.
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0.0) FROM LeaveBalanceLedger l " +
            "WHERE l.employee.employeeId = :employeeId AND l.leavePolicy.id = :leavePolicyId")
    double sumAmountByEmployeeAndPolicy(@Param("employeeId") String employeeId, @Param("leavePolicyId") Long leavePolicyId);

    /**
     * Calculates the total granted amount by summing only positive transactions.
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0.0) FROM LeaveBalanceLedger l " +
            "WHERE l.employee.employeeId = :employeeId AND l.leavePolicy.id = :leavePolicyId " +
            "AND l.amount > 0")
    double sumGrantsByEmployeeAndPolicy(@Param("employeeId") String employeeId, @Param("leavePolicyId") Long leavePolicyId);

    /**
     * Calculates the total used amount by summing only negative transactions.
     * The result will be a negative number or zero.
     */
    @Query("SELECT COALESCE(SUM(l.amount), 0.0) FROM LeaveBalanceLedger l " +
            "WHERE l.employee.employeeId = :employeeId AND l.leavePolicy.id = :leavePolicyId " +
            "AND l.amount < 0")
    double sumUsageByEmployeeAndPolicy(@Param("employeeId") String employeeId, @Param("leavePolicyId") Long leavePolicyId);

    /**
     * Finds all transactions of specific types (e.g., all accruals) for recalculation.
     */
    List<LeaveBalanceLedger> findByEmployee_EmployeeIdAndLeavePolicy_IdAndTransactionTypeIn(String employeeId, Long leavePolicyId, List<LeaveTransactionType> transactionTypes);

    /**
     * Deletes all transactions of specific types, used for recalculating grants.
     */
    void deleteByEmployee_EmployeeIdAndLeavePolicy_IdAndTransactionTypeIn(String employeeId, Long leavePolicyId, List<LeaveTransactionType> transactionTypes);

    // --- ADD THESE TWO METHODS TO FIX THE ERROR ---

    /**
     * Finds all transactions for a specific employee and policy on or before a given date.
     */
    List<LeaveBalanceLedger> findByEmployee_EmployeeIdAndLeavePolicy_IdAndTransactionDateLessThanEqualOrderByTransactionDateAsc(String employeeId, Long leavePolicyId, LocalDate asOfDate);

    /**
     * Finds all transactions for a specific employee on or before a given date.
     */
    List<LeaveBalanceLedger> findByEmployee_EmployeeIdAndTransactionDateLessThanEqualOrderByTransactionDateAsc(String employeeId, LocalDate asOfDate);
}
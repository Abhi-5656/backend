// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/repository/LeaveBalanceRepository.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployee_EmployeeId(String employeeId);
    Optional<LeaveBalance> findByEmployee_EmployeeIdAndLeavePolicy_Id(String employeeId, Long leavePolicyId);

    @Modifying
    @Query("UPDATE LeaveBalance lb SET lb.balance = :balance WHERE lb.employee.employeeId = :employeeId AND lb.leavePolicy.id = :leavePolicyId")
    void updateBalanceByEmployeeIdAndLeavePolicyId(@Param("employeeId") String employeeId, @Param("leavePolicyId") Long leavePolicyId, @Param("balance") double balance);
}
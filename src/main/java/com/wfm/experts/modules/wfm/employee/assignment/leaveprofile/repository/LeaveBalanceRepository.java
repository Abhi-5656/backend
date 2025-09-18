// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/repository/LeaveBalanceRepository.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployee_EmployeeId(String employeeId);
    Optional<LeaveBalance> findByEmployee_EmployeeIdAndLeavePolicy_Id(String employeeId, Long leavePolicyId);
}
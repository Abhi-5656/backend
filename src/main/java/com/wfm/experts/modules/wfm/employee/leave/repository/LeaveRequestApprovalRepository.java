package com.wfm.experts.modules.wfm.employee.leave.repository;

import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestApprovalRepository extends JpaRepository<LeaveRequestApproval, Long> {

    List<LeaveRequestApproval> findByApprover_EmployeeIdAndStatus(String employeeId, String status);

}
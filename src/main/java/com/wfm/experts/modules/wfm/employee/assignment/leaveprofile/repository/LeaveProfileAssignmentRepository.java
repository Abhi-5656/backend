package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveProfileAssignmentRepository extends JpaRepository<LeaveProfileAssignment, Long> {

    List<LeaveProfileAssignment> findByEmployeeId(String employeeId);
}
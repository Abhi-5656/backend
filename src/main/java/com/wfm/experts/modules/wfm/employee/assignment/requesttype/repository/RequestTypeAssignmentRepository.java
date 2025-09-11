package com.wfm.experts.modules.wfm.employee.assignment.requesttype.repository;

import com.wfm.experts.modules.wfm.employee.assignment.requesttype.entity.RequestTypeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestTypeAssignmentRepository extends JpaRepository<RequestTypeAssignment, Long> {

    List<RequestTypeAssignment> findByEmployeeId(String employeeId);
}
package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.entity.RequestTypeProfileAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestTypeProfileAssignmentRepository extends JpaRepository<RequestTypeProfileAssignment, Long> {

    List<RequestTypeProfileAssignment> findByEmployeeId(String employeeId);
}
package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.repository;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity.HolidayProfileAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HolidayProfileAssignmentRepository extends JpaRepository<HolidayProfileAssignment, Long> {

    /**
     * Retrieve all holiday profile assignments for a given employee ID.
     */
    List<HolidayProfileAssignment> findByEmployeeId(String employeeId);
}

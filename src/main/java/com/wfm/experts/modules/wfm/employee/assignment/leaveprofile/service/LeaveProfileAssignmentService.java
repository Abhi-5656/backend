package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import java.time.LocalDate;
import java.util.List;

public interface LeaveProfileAssignmentService {

    List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto);

    List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);

    List<LeaveProfileAssignmentDTO> getAllAssignments();

    /**
     * Sets the expiration date for an active assignment.
     * @param id The ID of the assignment to expire.
     * @param expirationDate The date it should expire on.
     */
    LeaveProfileAssignmentDTO expireAssignment(Long id, LocalDate expirationDate);

    /**
     * Deactivates an assignment, effectively soft-deleting it.
     * @param id The ID of the assignment to deactivate.
     */
    void deactivateAssignment(Long id);
}
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import java.util.List;

public interface LeaveProfileAssignmentService {
    List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto);
    List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);
    List<LeaveProfileAssignmentDTO> getAllAssignments();
}
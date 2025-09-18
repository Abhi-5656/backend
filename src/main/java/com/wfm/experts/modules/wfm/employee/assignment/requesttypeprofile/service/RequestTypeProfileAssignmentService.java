package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto.RequestTypeProfileAssignmentDTO;

import java.util.List;

public interface RequestTypeProfileAssignmentService {
    List<RequestTypeProfileAssignmentDTO> assignRequestTypeProfile(RequestTypeProfileAssignmentDTO dto);
    List<RequestTypeProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);
    List<RequestTypeProfileAssignmentDTO> getAllAssignments();
}
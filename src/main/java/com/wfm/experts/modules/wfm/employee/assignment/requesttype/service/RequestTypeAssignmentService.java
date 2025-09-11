package com.wfm.experts.modules.wfm.employee.assignment.requesttype.service;

import com.wfm.experts.modules.wfm.employee.assignment.requesttype.dto.RequestTypeAssignmentDTO;
import java.util.List;

public interface RequestTypeAssignmentService {
    List<RequestTypeAssignmentDTO> assignRequestType(RequestTypeAssignmentDTO dto);
    List<RequestTypeAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);
    List<RequestTypeAssignmentDTO> getAllAssignments();
}
package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.MultiShiftRotationAssignmentRequestDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;

import java.util.List;

public interface ShiftRotationAssignmentService {
//    ShiftRotationAssignment createAssignment(ShiftRotationAssignmentDTO dto);
    ShiftRotationAssignment updateAssignment(Long id, ShiftRotationAssignmentDTO dto);
    void deleteAssignment(Long id);
    ShiftRotationAssignment getAssignment(Long id);
    List<ShiftRotationAssignment> getAllAssignments();
    // Bulk assignment method
    // Assign a shift rotation to multiple employees in one call
    List<ShiftRotationAssignment> assignShiftRotationToMultipleEmployees(MultiShiftRotationAssignmentRequestDTO requestDTO);

}

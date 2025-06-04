package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;

import java.util.List;

public interface ShiftRotationAssignmentService {
    ShiftRotationAssignment createAssignment(ShiftRotationAssignmentDTO dto);
    ShiftRotationAssignment updateAssignment(Long id, ShiftRotationAssignmentDTO dto);
    void deleteAssignment(Long id);
    ShiftRotationAssignment getAssignment(Long id);
    List<ShiftRotationAssignment> getAllAssignments();
}

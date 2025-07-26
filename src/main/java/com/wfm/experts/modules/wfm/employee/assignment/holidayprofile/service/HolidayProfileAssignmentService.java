package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;

import java.util.List;

public interface HolidayProfileAssignmentService {

    /**
     * Assign the given HolidayProfile to all employee IDs in the DTO.
     * Returns one DTO per created assignment.
     */
    List<HolidayProfileAssignmentDTO> assignHolidayProfiles(HolidayProfileAssignmentDTO dto);

    /**
     * Retrieve all HolidayProfileAssignments.
     */
    List<HolidayProfileAssignmentDTO> getAllAssignments();

    /**
     * Retrieve all HolidayProfileAssignments for a given employee.
     */
    List<HolidayProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId);

    /**
     * Retrieve a single HolidayProfileAssignment by its ID.
     */
    HolidayProfileAssignmentDTO getAssignmentById(Long id);

    /**
     * Get all assigned holidays for a given employee.
     */
    List<HolidayDTO> getAssignedHolidaysByEmployeeId(String employeeId);

    /**
     * Delete (or deactivate) a HolidayProfileAssignment by its ID.
     */
    void deleteAssignment(Long id);
}

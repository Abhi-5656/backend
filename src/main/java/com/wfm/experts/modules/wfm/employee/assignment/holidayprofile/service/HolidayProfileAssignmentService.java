package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;

import java.time.LocalDate;
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
     * Deactivate a HolidayProfileAssignment by its ID (sets isActive = false).
     */
    void deactivateAssignment(Long id);

    /**
     * Sets the expiration date for an active assignment.
     * @param id The ID of the assignment to expire.
     * @param expirationDate The date it should expire on.
     */
    HolidayProfileAssignmentDTO expireAssignment(Long id, LocalDate expirationDate);
}
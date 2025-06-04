package com.wfm.experts.modules.wfm.features.roster.service;

import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeShiftService {

    /**
     * Generate employee shifts based on assigned shift rotation.
     *
     * @param employeeId The employee's ID
     * @param startDate  Start date for generation (inclusive)
     * @param endDate    End date for generation (inclusive)
     */
    void generateShiftsFromRotation(String employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get all EmployeeShiftDTOs for a given employee within the date range (inclusive).
     */
    List<EmployeeShiftDTO> getShiftsForEmployeeInRange(String employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Soft-delete an employee shift (never physically removes).
     * @param shiftId ID of the shift to soft-delete.
     */
    void softDeleteShift(Long shiftId);

    /**
     * Soft-update a shift (mark old as deleted, insert new row for audit trace).
     * @param shiftId  ID of the shift to update.
     * @param updatedBy Who is performing the update (for audit).
     */
    void softUpdateShift(Long shiftId, String updatedBy);
}

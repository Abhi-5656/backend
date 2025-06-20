package com.wfm.experts.modules.wfm.features.roster.service;

import com.wfm.experts.modules.wfm.features.roster.dto.BulkEmployeeShiftUpdateRequestDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterDTO;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeShiftService {

    /**
     * Generate employee shifts based on assigned shift rotation.
     *

     * @param startDate  Start date for generation (inclusive)
     * @param endDate    End date for generation (inclusive)
     */
//    void generateShiftsFromRotation(String employeeId, LocalDate startDate, LocalDate endDate);
    void generateShiftsFromRotation(List<String> employeeIds, LocalDate startDate, LocalDate endDate);

    /**
     * Get all EmployeeShiftDTOs for a given employee within the date range (inclusive).
     */

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

    /**
     * Get the roster view: all employees × dates in range, with or without assigned shift.
     * If an employee has no shift assigned for a date, shift is null.
     *
     * @param startDate Inclusive start date
     * @param endDate   Inclusive end date
     * @return List of EmployeeShiftRosterDTO (one per employee per date)
     */
    List<EmployeeShiftRosterDTO> getEmployeeRosterForDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Bulk assign or update a shift for multiple employees over multiple dates.
     *
     * If an EmployeeShift already exists for (employee, date), update the shift assignment.
     * If not, insert a new EmployeeShift row.
     *
     * @param request DTO containing employeeIds, calendarDates, shiftId, and assignedBy.
     */
    void bulkAssignOrUpdateShifts(BulkEmployeeShiftUpdateRequestDTO request);

    /**
     * Get roster view for a specific employee between start and end date.
     *
     * @param employeeId Employee ID
     * @param startDate  Start date (inclusive)
     * @param endDate    End date (inclusive)
     * @return List of EmployeeShiftRosterDTO
     */
    List<EmployeeShiftRosterDTO> getRosterForEmployeeByDateRange(String employeeId, LocalDate startDate, LocalDate endDate);


}


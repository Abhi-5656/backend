package com.wfm.experts.modules.wfm.features.timesheet.service;

import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimesheetService {

    TimesheetDTO createTimesheet(TimesheetDTO timesheetDTO);

    /**
     * Creates multiple timesheets in a single operation.
     * @param timesheetDTOs A list of timesheet data transfer objects.
     * @return A list of the created timesheet data transfer objects.
     */
    List<TimesheetDTO> createTimesheets(List<TimesheetDTO> timesheetDTOs);

    TimesheetDTO updateTimesheet(Long id, TimesheetDTO timesheetDTO);

    Optional<TimesheetDTO> getTimesheetById(Long id);

    Optional<TimesheetDTO> getTimesheetByEmployeeAndDate(String employeeId, LocalDate workDate);

    List<TimesheetDTO> getTimesheetsByEmployeeAndDateRange(String employeeId, LocalDate start, LocalDate end);

    void deleteTimesheet(Long id);
}

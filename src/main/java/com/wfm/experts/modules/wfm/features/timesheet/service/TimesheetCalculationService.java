package com.wfm.experts.modules.wfm.features.timesheet.service;

import java.time.LocalDate;

public interface TimesheetCalculationService {
    /**
     * Process all punch events for an employee on a given date,
     * applying the assigned pay policy and returning the result.
     * This method should be called after every punch save/update.
     */
    void processPunchEvents(String employeeId, LocalDate date);
}

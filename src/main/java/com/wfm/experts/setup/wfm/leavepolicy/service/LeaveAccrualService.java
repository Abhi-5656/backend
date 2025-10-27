// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/LeaveAccrualService.java
package com.wfm.experts.setup.wfm.leavepolicy.service;

import java.time.YearMonth;

public interface LeaveAccrualService {
    /**
     * Accrues leave for unconditional, scheduled grants (e.g., "Repeatedly").
     * This is typically called by a scheduler.
     *
     * @param month The month for which to run the accrual.
     */
    void accrueRepeatedGrant(YearMonth month);

    /**
     * Accrues leave for conditional, attendance-based grants (e.g., "Earned").
     * This is typically called after attendance is processed.
     *
     * @param month The month for which to run the accrual.
     */
    void accrueEarnedGrant(YearMonth month);

    /**
     * Recalculates the entire leave balance for an employee from the effective date of their leave profile assignment.
     *
     * @param employeeId The ID of the employee whose balance needs to be recalculated.
     */
    void recalculateTotalLeaveBalance(String employeeId);

    /**
     * NEW METHOD
     * Calculates and ADDS the earned grant for a single employee for a specific month.
     * This is triggered by the punch processing consumer to meet your requirement.
     *
     * @param employeeId The ID of the employee.
     * @param month The month to process.
     */
    void incrementEarnedGrantForMonth(String employeeId, YearMonth month);
}
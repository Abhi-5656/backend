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
}
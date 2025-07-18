package com.wfm.experts.setup.wfm.paypolicy.enums;

/**
 * Defines the compensation policy for working on a weekend.
 */
public enum WeekendPayType {
    /**
     * Employee is paid for the hours worked, possibly at a different rate.
     */
    PAID_ONLY,
    /**
     * Employee is paid for the hours worked and also receives compensatory time off.
     */
    PAID_AND_COMP_OFF
}
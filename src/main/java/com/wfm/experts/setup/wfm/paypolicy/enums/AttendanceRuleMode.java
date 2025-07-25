package com.wfm.experts.setup.wfm.paypolicy.enums;

/**
 * Distinguishes the mode of operation for an attendance rule,
 * either based on absolute time (UNSCHEDULED) or relative to a shift (SCHEDULED).
 */
public enum AttendanceRuleMode {
    /**
     * Rules are based on a fixed number of hours/minutes worked,
     * regardless of any scheduled shift.
     */
    UNSCHEDULED,

    /**
     * Rules are based on the percentage of a scheduled shift's duration that was worked.
     */
    SCHEDULED
}
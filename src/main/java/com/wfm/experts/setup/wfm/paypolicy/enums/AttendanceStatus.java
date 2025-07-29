package com.wfm.experts.setup.wfm.paypolicy.enums;

/**
 * Represents the possible attendance statuses for a work day.
 * Using an enum ensures type safety and adherence to standard naming conventions for constants.
 */
public enum AttendanceStatus {
    /**
     * Indicates the employee was present for a full day.
     */
    PRESENT,

    /**
     * Indicates the employee was absent.
     */
    ABSENT,

    /**
     * Indicates the employee was present for a half day.
     */
    HALF_DAY,

    /**
     * Indicates that at least one punch exists, but a corresponding IN or OUT punch is missing.
     */
    PENDING
}
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums;

/**
 * Defines the types of transactions that can be written to the leave balance ledger.
 */
public enum LeaveTransactionType {
    ACCRUAL,                 // Scheduled grant (e..g., monthly, yearly)
    ACCRUAL_RECALCULATION,   // A grant from a full recalculation
    MANUAL_ADJUSTMENT,       // Admin override
    USAGE_APPLIED,           // Leave request was approved and used
    USAGE_REVERSAL_REJECTED, // Leave request was rejected, balance returned
    USAGE_REVERSAL_CANCELLED,// Leave request was cancelled, balance returned
    EXPIRATION,              // Balance expired (negative transaction)
    CARRYOVER_GRANT          // Balance carried over (positive transaction)
}
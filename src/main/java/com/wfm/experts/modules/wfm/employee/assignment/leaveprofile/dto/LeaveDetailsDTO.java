package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing a single transaction in the leave ledger, combined with
 * summary data from the leave balance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveDetailsDTO {

    // Fields from LeaveBalanceLedger (Transaction)
    private Long id; // This is the Ledger transaction ID
    private Long leavePolicyId;
    private String leavePolicyName;
    private LeaveTransactionType transactionType;
    private double amount;
    private LocalDate transactionDate;

    // Fields from LeaveBalance (Summary)
    private double currentBalance;
    private double totalGranted;
    private double usedBalance;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private LocalDate lastAccrualDate;
    private LocalDate nextAccrualDate;
    private String status;
}
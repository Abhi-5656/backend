package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing a single transaction in the leave ledger.
 * This is used to show the "details" of leave history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveDetailsDTO {
    private Long id;
    private String employeeId;
    private Long leavePolicyId;
    private String leavePolicyName;
    private LeaveTransactionType transactionType;
    private double amount;
    private LocalDate transactionDate;
    private String notes;
    private Long relatedRequestId;
}
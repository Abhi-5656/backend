// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/dto/LeaveBalanceDTO.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Import LocalDate

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDTO {
    private String leavePolicyName;
    private double balance;
    private LocalDate effectiveDate; // Add effectiveDate
    private LocalDate expirationDate; // Add expirationDate
}
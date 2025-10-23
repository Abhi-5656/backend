// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/dto/LeavePolicyBalanceDTO.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import lombok.Data;
import java.time.LocalDate; // Import

@Data
public class LeavePolicyBalanceDTO {
    private Long id; // This is the LeavePolicy ID
    private double balance;
    private LocalDate effectiveDate; // Add effectiveDate
    private LocalDate expirationDate; // Add expirationDate (can be null)
}
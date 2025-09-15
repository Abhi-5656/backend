// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/dto/LeaveBalanceDTO.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDTO {
    private String leavePolicyName;
    private double balance;
}
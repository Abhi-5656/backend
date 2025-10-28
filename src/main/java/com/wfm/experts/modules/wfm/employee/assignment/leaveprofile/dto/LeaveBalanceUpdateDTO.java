package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import java.util.List;
import lombok.Data;

@Data
public class LeaveBalanceUpdateDTO {
    private String employeeId; // Changed from List<String> to String
    private List<LeavePolicyBalanceDTO> leavePolicies;
}
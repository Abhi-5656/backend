package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import java.util.List;
import lombok.Data;

@Data
public class LeaveBalanceUpdateDTO {
    private List<String> employeeIds;
    private List<LeavePolicyBalanceDTO> leavePolicies;
}
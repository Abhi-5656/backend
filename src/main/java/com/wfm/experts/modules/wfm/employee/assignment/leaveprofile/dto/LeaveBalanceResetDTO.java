package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import java.util.List;
import lombok.Data;

@Data
public class LeaveBalanceResetDTO {
    private List<String> employeeIds;
    private List<Long> leavePolicyIds;
}
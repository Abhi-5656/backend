// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/service/LeaveBalanceService.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceResetDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;

import java.util.List;

public interface LeaveBalanceService {
    List<LeaveBalanceDTO> getLeaveBalances(String employeeId);

//    void resetLeaveBalances(LeaveBalanceResetDTO resetDTO);

//    void updateLeaveBalances(LeaveBalanceUpdateDTO updateDTO);
   void updateLeaveBalances(List<LeaveBalanceUpdateDTO> updateDTOs);
}
// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/service/LeaveBalanceService.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceResetDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveDetailsDTO;

import java.time.LocalDate;
import java.util.List;

public interface LeaveBalanceService {
    List<LeaveBalanceDTO> getLeaveBalances(String employeeId);

//    void resetLeaveBalances(LeaveBalanceResetDTO resetDTO);

//    void updateLeaveBalances(LeaveBalanceUpdateDTO updateDTO);
   void updateLeaveBalances(List<LeaveBalanceUpdateDTO> updateDTOs);

    /**
     * Gets the leave ledger transaction history (details) for an employee up to a specific date.
     * Can be filtered by a specific leave policy.
     *
     * @param employeeId   The employee's ID.
     * @param asOfDate     The date filter.
     * @param leavePolicyId Optional. If provided, filters for a specific leave policy.
     * @return A list of leave ledger transactions.
     */
    List<LeaveDetailsDTO> getLeaveDetailsAsOf(String employeeId, LocalDate asOfDate, Long leavePolicyId);
}
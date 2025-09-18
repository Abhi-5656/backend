package com.wfm.experts.modules.wfm.employee.leave.service;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestDTO applyForLeave(LeaveRequestDTO leaveRequestDTO);

    List<LeaveRequestApprovalDTO> getPendingApprovals(String approverId);

    LeaveRequestActionResponseDTO approveOrRejectLeave(Long approvalId, String approverId, boolean approved);

    LeaveRequestActionResponseDTO cancelLeaveRequest(Long leaveRequestId, String employeeId);
}
package com.wfm.experts.modules.wfm.employee.leave.controller;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-approvals")
@RequiredArgsConstructor
public class LeaveRequestApprovalController {
    private final LeaveRequestService leaveRequestService;

    @GetMapping("/pending/{approverId}")
    public ResponseEntity<List<LeaveRequestApprovalDTO>> getPendingApprovals(@PathVariable String approverId) {
        return ResponseEntity.ok(leaveRequestService.getPendingApprovals(approverId));
    }

    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<LeaveRequestActionResponseDTO> approveLeaveRequest(@PathVariable Long approvalId, @RequestParam String approverId) {
        return ResponseEntity.ok(leaveRequestService.approveOrRejectLeave(approvalId, approverId, true));
    }

    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<LeaveRequestActionResponseDTO> rejectLeaveRequest(@PathVariable Long approvalId, @RequestParam String approverId) {
        return ResponseEntity.ok(leaveRequestService.approveOrRejectLeave(approvalId, approverId, false));
    }
}
package com.wfm.experts.modules.wfm.employee.leave.controller;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping("/apply")
    public ResponseEntity<LeaveRequestDTO> applyForLeave(@RequestBody LeaveRequestDTO leaveRequestDTO) {
        return ResponseEntity.ok(leaveRequestService.applyForLeave(leaveRequestDTO));
    }

    @PostMapping("/{leaveRequestId}/cancel")
    public ResponseEntity<LeaveRequestActionResponseDTO> cancelLeaveRequest(
            @PathVariable Long leaveRequestId,
            @RequestParam String employeeId) {
        return ResponseEntity.ok(leaveRequestService.cancelLeaveRequest(leaveRequestId, employeeId));
    }
}
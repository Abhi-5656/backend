package com.wfm.experts.modules.wfm.employee.leave.controller;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
import com.wfm.experts.modules.wfm.employee.leave.enums.LeaveStatus;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;

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

    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaveRequests(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) String status) {

        // 1) Try to read employeeId from JWT claim
        String employeeId = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                employeeId = jwtUtil.extractEmployeeId(token);
            } catch (Exception ignored) { /* fall through to email fallback */ }
        }

        // 2) Fallback: resolve employeeId via email from Authentication
        if (employeeId == null || employeeId.isBlank()) {
            String email = (authentication != null) ? authentication.getName() : null;
            if (email != null && !email.isBlank()) {
                EmployeeDTO emp = employeeService.getEmployeeByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Employee not found for email: " + email));
                employeeId = emp.getEmployeeId();
            } else {
                throw new RuntimeException("Unable to resolve employeeId (no JWT claim and no auth email).");
            }
        }

        // 3) Filter by status if provided; otherwise return all
        if (status != null && !status.trim().isEmpty()) {
            try {
                LeaveStatus leaveStatus = LeaveStatus.valueOf(status.trim().toUpperCase());
                return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveRequestsByStatus(employeeId, leaveStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status -> return all (keeps UI from breaking)
                return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveRequests(employeeId));
            }
        } else {
            return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveRequests(employeeId));
        }
    }
}
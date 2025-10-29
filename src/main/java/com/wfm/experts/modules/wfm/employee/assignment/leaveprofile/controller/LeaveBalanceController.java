package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveDetailsDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; // <-- Import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // <-- Import
import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<LeaveBalanceDTO>> getLeaveBalances(@PathVariable String employeeId) {
        return ResponseEntity.ok(leaveBalanceService.getLeaveBalances(employeeId));
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateLeaveBalances(@RequestBody List<LeaveBalanceUpdateDTO> updateDTOs) {
        leaveBalanceService.updateLeaveBalances(updateDTOs);
        return ResponseEntity.ok().build();
    }

    // --- NEW ENDPOINT ---
    /**
     * Gets the leave ledger transaction history (details) for an employee up to a specific date.
     *
     * @param employeeId    The employee's ID.
     * @param asOfDate      The "as of" date filter (YYYY-MM-DD).
     * @param leavePolicyId Optional. The specific leave policy ID to filter for.
     * @return A list of leave ledger transactions.
     */
    @GetMapping("/leave-details/{employeeId}")
    public ResponseEntity<List<LeaveDetailsDTO>> getLeaveDetails(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) Long leavePolicyId) {

        List<LeaveDetailsDTO> details = leaveBalanceService.getLeaveDetailsAsOf(employeeId, asOfDate, leavePolicyId);
        return ResponseEntity.ok(details);
    }
    // --- END OF NEW ENDPOINT ---
}
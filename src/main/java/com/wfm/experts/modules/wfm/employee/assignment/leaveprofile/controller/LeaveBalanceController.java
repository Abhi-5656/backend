// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/controller/LeaveBalanceController.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceResetDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> updateLeaveBalances(@RequestBody LeaveBalanceUpdateDTO updateDTO) {
        leaveBalanceService.updateLeaveBalances(updateDTO);
        return ResponseEntity.ok().build();
    }
}
// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/service/impl/LeaveBalanceServiceImpl.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceResetDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeavePolicyBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveBalanceMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveBalanceService;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceMapper leaveBalanceMapper;
    // Add dependencies needed for the new logic
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;

    @Override
    public List<LeaveBalanceDTO> getLeaveBalances(String employeeId) {
        List<LeaveBalance> leaveBalances = leaveBalanceRepository.findByEmployee_EmployeeId(employeeId);
        return leaveBalances.stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void updateLeaveBalances(LeaveBalanceUpdateDTO updateDTO) {
        for (String employeeId : updateDTO.getEmployeeIds()) {
            // Find the employee first (or throw)
            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

            for (LeavePolicyBalanceDTO policyBalance : updateDTO.getLeavePolicies()) {
                // Find the policy first (or throw)
                LeavePolicy leavePolicy = leavePolicyRepository.findById(policyBalance.getId())
                        .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + policyBalance.getId()));

                // Find existing balance record or create a new one
                LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(
                        employeeId,
                        policyBalance.getId()
                ).orElse(LeaveBalance.builder()
                        .employee(employee)
                        .leavePolicy(leavePolicy)
                        .currentBalance(0)  // <-- NEW: Initialize
                        .totalGranted(0)    // <-- NEW: Initialize
                        .usedBalance(0)     // <-- NEW: Initialize
                        .status("ACTIVE")   // <-- NEW: Initialize
                        .build());

                // --- UPDATED LOGIC for Manual Override ---

                double newCurrentBalance = policyBalance.getBalance();
                double oldCurrentBalance = leaveBalance.getCurrentBalance();

                // Calculate the adjustment delta (the amount the admin is adding or removing)
                double adjustmentDelta = newCurrentBalance - oldCurrentBalance;

                // Apply the new balance
                leaveBalance.setCurrentBalance(newCurrentBalance);

                // Adjust total_granted by the same delta. This keeps the math consistent:
                // (newTotalGranted) - (usedBalance) = (newCurrentBalance)
                // (oldTotalGranted + delta) - (usedBalance) = (oldCurrentBalance + delta)
                leaveBalance.setTotalGranted(leaveBalance.getTotalGranted() + adjustmentDelta);

                // Set status to manual override to stop future accruals
                leaveBalance.setStatus("MANUAL_OVERRIDE");

                // Set the manual effective and expiration dates from the DTO
                // If effectiveDate is null, default to today. ExpirationDate can be null.
                leaveBalance.setEffectiveDate(policyBalance.getEffectiveDate() != null ? policyBalance.getEffectiveDate() : LocalDate.now());
                leaveBalance.setExpirationDate(policyBalance.getExpirationDate());

                // Nullify accrual dates as they are not relevant for a manual balance
                leaveBalance.setLastAccrualDate(null);
                leaveBalance.setNextAccrualDate(null);

                leaveBalanceRepository.save(leaveBalance);
                // --- END OF UPDATE ---
            }
        }
    }
}
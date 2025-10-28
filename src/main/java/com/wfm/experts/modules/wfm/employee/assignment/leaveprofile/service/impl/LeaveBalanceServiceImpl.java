package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceResetDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeavePolicyBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveBalanceMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceLedgerRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceLedgerRepository leaveBalanceLedgerRepository; // Injected
    private final LeaveBalanceMapper leaveBalanceMapper;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;

    /**
     * Reads the pre-calculated balances from the summary table.
     * This is fast and is used by dashboards and UIs.
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<LeaveBalanceDTO> getLeaveBalances(String employeeId) {
        // This method now simply reads the summary table
        List<LeaveBalance> leaveBalances = leaveBalanceRepository.findByEmployee_EmployeeId(employeeId);
        return leaveBalances.stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Manually overrides an employee's leave balance.
     * This writes a new "adjustment" transaction to the ledger, sets the summary
     * status to "MANUAL_ADJUSTMENT", and updates the summary values.
     */
    @Override
    @Transactional
    public void updateLeaveBalances(List<LeaveBalanceUpdateDTO> updateDTOs) {

        // Iterate over the list of DTOs, where each DTO is for one employee
        for (LeaveBalanceUpdateDTO updateDTO : updateDTOs) {

            // Get the single employeeId from this specific DTO
            String employeeId = updateDTO.getEmployeeId();

            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

            // Apply all policy updates for this specific employee
            for (LeavePolicyBalanceDTO policyBalanceDto : updateDTO.getLeavePolicies()) {
                LeavePolicy leavePolicy = leavePolicyRepository.findById(policyBalanceDto.getId())
                        .orElseThrow(() -> new RuntimeException("LeavePolicy not found: " + policyBalanceDto.getId()));

                LeaveBalance leaveBalanceMetadata = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(
                        employeeId,
                        policyBalanceDto.getId()
                ).orElseThrow(() -> new IllegalStateException("LeaveBalance metadata row not found. Cannot manually adjust."));

                // 1. The DTO's 'balance' field IS the adjustment amount.
                double adjustmentAmount = policyBalanceDto.getBalance(); // e.g., 5.0 or -2.0

                // 2. Create the new ledger transaction
                LeaveBalanceLedger ledgerEntry = LeaveBalanceLedger.builder()
                        .employee(employee)
                        .leavePolicy(leavePolicy)
                        .transactionType(LeaveTransactionType.MANUAL_ADJUSTMENT)
                        .amount(adjustmentAmount) // The amount to add/subtract
                        // --- THIS IS THE FIX ---
                        // The transaction date is when the adjustment is made (now).
                        .transactionDate(LocalDate.now())
                        .notes("Manual balance adjustment by admin. Amount: " + adjustmentAmount)
                        .build();
                leaveBalanceLedgerRepository.save(ledgerEntry);

                // 3. Update the summary table (METADATA + MANUAL BALANCE)
                //    The effectiveDate from the DTO controls when this override *applies* from.
                leaveBalanceMetadata.setStatus("MANUAL_ADJUSTMENT");
                leaveBalanceMetadata.setEffectiveDate(policyBalanceDto.getEffectiveDate() != null ? policyBalanceDto.getEffectiveDate() : LocalDate.now());
                leaveBalanceMetadata.setExpirationDate(policyBalanceDto.getExpirationDate());
                leaveBalanceMetadata.setLastAccrualDate(null); // Stop automated accruals
                leaveBalanceMetadata.setNextAccrualDate(null);

                // 4. Manually set the summary fields to the new target values by re-reading the ledger
                double newTotalGranted = leaveBalanceLedgerRepository.sumGrantsByEmployeeAndPolicy(employeeId, leavePolicy.getId());
                double newUsedBalance = Math.abs(leaveBalanceLedgerRepository.sumUsageByEmployeeAndPolicy(employeeId, leavePolicy.getId()));
                double newCurrentBalance = leaveBalanceLedgerRepository.sumAmountByEmployeeAndPolicy(employeeId, leavePolicy.getId());

                leaveBalanceMetadata.setCurrentBalance(newCurrentBalance);
                leaveBalanceMetadata.setTotalGranted(newTotalGranted);
                leaveBalanceMetadata.setUsedBalance(newUsedBalance);

                leaveBalanceRepository.save(leaveBalanceMetadata);
            }
        }
    }
}
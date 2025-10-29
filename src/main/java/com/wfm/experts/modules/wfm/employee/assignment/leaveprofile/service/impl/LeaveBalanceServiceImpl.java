package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceUpdateDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveDetailsDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeavePolicyBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveBalanceMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveDetailsMapper;
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
import java.util.ArrayList; // Import ArrayList
import java.util.Comparator; // Import Comparator
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceLedgerRepository leaveBalanceLedgerRepository;
    private final LeaveBalanceMapper leaveBalanceMapper;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveDetailsMapper leaveDetailsMapper;

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

    /**
     * Gets the leave ledger transaction history (details) for an employee up to a specific date.
     * This method now calculates a "running balance" for each transaction, like a bank statement.
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<LeaveDetailsDTO> getLeaveDetailsAsOf(String employeeId, LocalDate asOfDate, Long leavePolicyId) {

        List<LeaveBalanceLedger> ledgerEntries;

        // 1. Fetch all balance summaries for the employee (to get static metadata like effective date, status)
        Map<Long, LeaveBalance> balanceMap = leaveBalanceRepository.findByEmployee_EmployeeId(employeeId).stream()
                .collect(Collectors.toMap(b -> b.getLeavePolicy().getId(), b -> b));

        // 2. Fetch the requested ledger (transaction) entries
        if (leavePolicyId != null) {
            ledgerEntries = leaveBalanceLedgerRepository
                    .findByEmployee_EmployeeIdAndLeavePolicy_IdAndTransactionDateLessThanEqualOrderByTransactionDateAsc(
                            employeeId, leavePolicyId, asOfDate);
        } else {
            ledgerEntries = leaveBalanceLedgerRepository
                    .findByEmployee_EmployeeIdAndTransactionDateLessThanEqualOrderByTransactionDateAsc(
                            employeeId, asOfDate);
        }

        // --- "Bank Statement" Logic ---
        List<LeaveDetailsDTO> results = new ArrayList<>();

        // Group transactions by policy to calculate running balances separately for each policy
        Map<Long, List<LeaveBalanceLedger>> entriesByPolicy = ledgerEntries.stream()
                .collect(Collectors.groupingBy(l -> l.getLeavePolicy().getId()));

        for (Map.Entry<Long, List<LeaveBalanceLedger>> entry : entriesByPolicy.entrySet()) {
            Long policyId = entry.getKey();
            List<LeaveBalanceLedger> policyTransactions = entry.getValue();

            // Get the static summary data for this policy
            LeaveBalance summary = balanceMap.get(policyId);

            // Initialize running totals for *this policy*
            double runningCurrentBalance = 0.0;
            double runningTotalGranted = 0.0;
            double runningUsedBalance = 0.0;

            for (LeaveBalanceLedger ledger : policyTransactions) {
                // Calculate running totals *before* creating the DTO
                double amount = ledger.getAmount();
                if (amount > 0) {
                    runningTotalGranted += amount;
                } else {
                    runningUsedBalance += Math.abs(amount);
                }
                runningCurrentBalance += amount;

                // Create the DTO
                LeaveDetailsDTO dto = new LeaveDetailsDTO();

                // Map fields from the transaction (Ledger)
                leaveDetailsMapper.updateFromLedger(ledger, dto);

                // Map static fields from the summary (Balance)
                if (summary != null) {
                    leaveDetailsMapper.updateFromBalance(summary, dto);
                }

                // *** This is the key fix ***
                // Manually set the calculated running balances, overriding
                // any stale data that might have been mapped from the 'summary' object.
                dto.setCurrentBalance(runningCurrentBalance);
                dto.setTotalGranted(runningTotalGranted);
                dto.setUsedBalance(runningUsedBalance);

                results.add(dto);
            }
        }

        // Sort the final combined list by date, as the grouping might mess up the order
        results.sort(Comparator.comparing(LeaveDetailsDTO::getTransactionDate).thenComparing(LeaveDetailsDTO::getId));

        return results;
    }
}
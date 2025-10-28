// REVISED FILE:
// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/impl/LeaveAccrualServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceLedgerRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.engine.executor.LeavePolicyRuleExecutor;
import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfilePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantType;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveAccrualServiceImpl implements LeaveAccrualService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveAccrualServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final LeaveProfileAssignmentRepository leaveProfileAssignmentRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveBalanceRepository leaveBalanceRepository; // The Summary table
    private final LeaveBalanceLedgerRepository leaveBalanceLedgerRepository; // The Ledger table
    private final LeavePolicyRuleExecutor ruleExecutor;
    private final LeavePolicyRepository leavePolicyRepository;

    // This is injected because the ruleExecutor uses rules that depend on it.
    private final TimesheetRepository timesheetRepository;

    /**
     * Helper to build the execution context
     */
    private LeavePolicyExecutionContext buildContext(Employee employee, LeavePolicy policy, YearMonth month) {
        return LeavePolicyExecutionContext.builder()
                .employee(employee)
                .leavePolicy(policy)
                .facts(new HashMap<>())
                .processingMonth(month)
                .build();
    }

    /**
     * Helper to get policies from a profile
     */
    private List<LeavePolicy> getLeavePoliciesFromProfile(LeaveProfile leaveProfile) {
        return leaveProfile.getLeaveProfilePolicies().stream()
                .map(LeaveProfilePolicy::getLeavePolicy)
                .collect(Collectors.toList());
    }

    /**
     * Helper to get or create a balance *summary* record
     */
    private LeaveBalance getOrCreateBalanceMetadata(Employee employee, LeavePolicy policy, LeaveProfileAssignment assignment) {
        return leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), policy.getId())
                .orElse(LeaveBalance.builder()
                        .employee(employee)
                        .leavePolicy(policy)
                        .status("ACTIVE")
                        .effectiveDate(assignment.getEffectiveDate())
                        .expirationDate(assignment.getExpirationDate())
                        .assignment(assignment)
                        .currentBalance(0.0) // Initialize all to 0
                        .totalGranted(0.0)
                        .usedBalance(0.0)
                        .build());
    }

    /**
     * NEW (REQUIRED)
     * Recalculates and saves the summary in employee_leave_balances
     * by summing all transactions from employee_leave_ledger.
     */
    @Override
    @Transactional
    public void updateSummaryBalance(String employeeId, Long leavePolicyId) {
        LeaveBalance leaveBalanceMetadata = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employeeId, leavePolicyId)
                .orElseThrow(() -> new IllegalStateException("LeaveBalance metadata row not found for employee " + employeeId + " and policy " + leavePolicyId));

        // If manually overridden, the summary is "frozen" and should not be updated by automated processes.
        if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalanceMetadata.getStatus())) {
            logger.debug("Skipping summary update for {}/{} because it is manually overridden.", employeeId, leavePolicyId);
            return;
        }

        // Calculate new summaries from the ledger
        double newTotalGranted = leaveBalanceLedgerRepository.sumGrantsByEmployeeAndPolicy(employeeId, leavePolicyId);
        double newUsedBalance = Math.abs(leaveBalanceLedgerRepository.sumUsageByEmployeeAndPolicy(employeeId, leavePolicyId));
        double newCurrentBalance = newTotalGranted - newUsedBalance; // Or sumAmountByEmployeeAndPolicy

        // Update the summary table
        leaveBalanceMetadata.setTotalGranted(newTotalGranted);
        leaveBalanceMetadata.setUsedBalance(newUsedBalance);
        leaveBalanceMetadata.setCurrentBalance(newCurrentBalance);

        leaveBalanceRepository.save(leaveBalanceMetadata);
    }

    // --- UPDATED SCHEDULER METHODS ---

    @Override
    @Transactional
    public void accrueRepeatedGrant(YearMonth month) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                    .stream().filter(LeaveProfileAssignment::isActive).findFirst()
                    .ifPresent(assignment -> {
                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) return;

                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                        if (leaveProfile == null) return;

                        for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {
                            if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {

                                LeaveBalance leaveBalanceMetadata = getOrCreateBalanceMetadata(employee, leavePolicy, assignment);

                                if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalanceMetadata.getStatus())) continue;

                                LocalDate lastAccrual = leaveBalanceMetadata.getLastAccrualDate();
                                if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) continue;

                                LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                double earnedLeave = ruleExecutor.execute(context);

                                if (earnedLeave > 0) {
                                    LeaveBalanceLedger ledgerEntry = LeaveBalanceLedger.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .transactionType(LeaveTransactionType.ACCRUAL)
                                            .amount(earnedLeave)
                                            .transactionDate(month.atDay(1))
                                            .notes("Scheduled repeated grant for " + month)
                                            .build();
                                    leaveBalanceLedgerRepository.save(ledgerEntry);

                                    leaveBalanceMetadata.setLastAccrualDate(month.atDay(1));
                                    leaveBalanceMetadata.setNextAccrualDate(month.plusMonths(1).atDay(1)); // TODO: Make smarter
                                    leaveBalanceRepository.save(leaveBalanceMetadata);

                                    // Update the summary table
                                    updateSummaryBalance(employee.getEmployeeId(), leavePolicy.getId());
                                }
                            }
                        }
                    });
        }
    }


    @Override
    @Transactional
    public void accrueEarnedGrant(YearMonth month) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                    .stream().filter(LeaveProfileAssignment::isActive).findFirst()
                    .ifPresent(assignment -> {
                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) return;

                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                        if (leaveProfile == null) return;

                        for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {
                            if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                                LeaveBalance leaveBalanceMetadata = getOrCreateBalanceMetadata(employee, leavePolicy, assignment);
                                if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalanceMetadata.getStatus())) continue;

                                LocalDate lastAccrual = leaveBalanceMetadata.getLastAccrualDate();
                                if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) continue;

                                LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                double earnedLeave = ruleExecutor.execute(context);

                                if (earnedLeave > 0) {
                                    LeaveBalanceLedger ledgerEntry = LeaveBalanceLedger.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .transactionType(LeaveTransactionType.ACCRUAL)
                                            .amount(earnedLeave)
                                            .transactionDate(month.atEndOfMonth())
                                            .notes("Scheduled earned grant for " + month)
                                            .build();
                                    leaveBalanceLedgerRepository.save(ledgerEntry);

                                    leaveBalanceMetadata.setLastAccrualDate(month.atEndOfMonth());
                                    leaveBalanceMetadata.setNextAccrualDate(month.plusMonths(1).atDay(1));
                                    leaveBalanceRepository.save(leaveBalanceMetadata);

                                    updateSummaryBalance(employee.getEmployeeId(), leavePolicy.getId());
                                }
                            }
                        }
                    });
        }
    }

    /**
     * This is the "destructive" true-up method. It clears old accruals
     * and rebuilds them, then updates the summary.
     */
    @Override
    @Transactional
    public void recalculateTotalLeaveBalance(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream().filter(LeaveProfileAssignment::isActive).findFirst()
                .ifPresent(assignment -> {
                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                    if (leaveProfile == null) return;

                    for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {

                        LocalDate today = LocalDate.now();
                        LeaveBalance leaveBalanceMetadata = getOrCreateBalanceMetadata(employee, leavePolicy, assignment);

                        // If manually overridden, check for expiration
                        if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalanceMetadata.getStatus())) {
                            if (leaveBalanceMetadata.getExpirationDate() != null && leaveBalanceMetadata.getExpirationDate().isBefore(today)) {
                                leaveBalanceMetadata.setStatus("ACTIVE"); // Override expired, proceed with recalc
                            } else {
                                continue; // Manual override is active, skip recalculation
                            }
                        }

                        // --- RECALCULATION LOGIC ---
                        LocalDate newLastAccrual = null;
                        LocalDate newNextAccrual = null;
                        YearMonth calculationStartMonth = YearMonth.from(assignment.getEffectiveDate());
                        YearMonth endMonth = YearMonth.from(today);

                        // 1. Delete all old accrual/recalculation transactions
                        List<LeaveTransactionType> grantTypes = Arrays.asList(LeaveTransactionType.ACCRUAL, LeaveTransactionType.ACCRUAL_RECALCULATION);
                        leaveBalanceLedgerRepository.deleteByEmployee_EmployeeIdAndLeavePolicy_IdAndTransactionTypeIn(employeeId, leavePolicy.getId(), grantTypes);

                        // 2. Re-run all grant rules from the beginning and create new ledger entries
                        if (leavePolicy.getGrantsConfig() != null) {
                            if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
                                if (!calculationStartMonth.isAfter(endMonth)) {
                                    for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                        LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                        double grant = ruleExecutor.execute(context);
                                        if(grant > 0) {
                                            newLastAccrual = month.atEndOfMonth();
                                            leaveBalanceLedgerRepository.save(LeaveBalanceLedger.builder()
                                                    .employee(employee).leavePolicy(leavePolicy)
                                                    .transactionType(LeaveTransactionType.ACCRUAL_RECALCULATION)
                                                    .amount(grant).transactionDate(month.atEndOfMonth())
                                                    .notes("Recalculated earned grant for " + month).build());
                                        }
                                    }
                                }
                                newNextAccrual = today.plusMonths(1).withDayOfMonth(1);

                            } else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
                                FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
                                if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {
                                    GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
                                    if (grantPeriod == GrantPeriod.YEARLY) {
                                        for (int year = calculationStartMonth.getYear(); year <= endMonth.getYear(); year++) {
                                            LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, YearMonth.of(year, 1));
                                            double grant = ruleExecutor.execute(context);
                                            if (grant > 0) {
                                                newLastAccrual = LocalDate.of(year, 1, 1);
                                                leaveBalanceLedgerRepository.save(LeaveBalanceLedger.builder()
                                                        .employee(employee).leavePolicy(leavePolicy)
                                                        .transactionType(LeaveTransactionType.ACCRUAL_RECALCULATION)
                                                        .amount(grant).transactionDate(LocalDate.of(year, 1, 1))
                                                        .notes("Recalculated yearly grant for " + year).build());
                                            }
                                        }
                                        newNextAccrual = today.plusYears(1).withDayOfYear(1);
                                    } else { // Monthly or PayPeriod
                                        if (!calculationStartMonth.isAfter(endMonth)) {
                                            for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                                LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                                double grant = ruleExecutor.execute(context);
                                                if(grant > 0) {
                                                    newLastAccrual = month.atDay(1);
                                                    leaveBalanceLedgerRepository.save(LeaveBalanceLedger.builder()
                                                            .employee(employee).leavePolicy(leavePolicy)
                                                            .transactionType(LeaveTransactionType.ACCRUAL_RECALCULATION)
                                                            .amount(grant).transactionDate(month.atDay(1))
                                                            .notes("Recalculated monthly grant for " + month).build());
                                                }
                                            }
                                        }
                                        newNextAccrual = today.plusMonths(1).withDayOfMonth(1);
                                    }
                                } else { // One-Time
                                    LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, calculationStartMonth);
                                    double grant = ruleExecutor.execute(context);
                                    if(grant > 0) {
                                        newLastAccrual = assignment.getEffectiveDate();
                                        leaveBalanceLedgerRepository.save(LeaveBalanceLedger.builder()
                                                .employee(employee).leavePolicy(leavePolicy)
                                                .transactionType(LeaveTransactionType.ACCRUAL_RECALCULATION)
                                                .amount(grant).transactionDate(newLastAccrual)
                                                .notes("Recalculated one-time grant").build());
                                    }
                                }
                            }
                        }

                        // 3. Update metadata
                        leaveBalanceMetadata.setLastAccrualDate(newLastAccrual);
                        leaveBalanceMetadata.setNextAccrualDate(newNextAccrual);
                        leaveBalanceMetadata.setStatus("ACTIVE");
                        leaveBalanceMetadata.setEffectiveDate(assignment.getEffectiveDate());
                        leaveBalanceMetadata.setExpirationDate(assignment.getExpirationDate());
                        leaveBalanceRepository.save(leaveBalanceMetadata);

                        // 4. Update the summary table with the newly rebuilt totals
                        updateSummaryBalance(employeeId, leavePolicy.getId());
                    }
                });
    }


    @Override
    @Transactional
    public void incrementEarnedGrantForMonth(String employeeId, YearMonth month) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElse(null);
        if (employee == null) return;

        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream().filter(LeaveProfileAssignment::isActive).findFirst()
                .ifPresent(assignment -> {
                    if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) return;

                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                    if (leaveProfile == null) return;

                    for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {
                        if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                            LeaveBalance leaveBalanceMetadata = getOrCreateBalanceMetadata(employee, leavePolicy, assignment);
                            if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalanceMetadata.getStatus())) continue;

                            LocalDate lastAccrual = leaveBalanceMetadata.getLastAccrualDate();
                            if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) continue;

                            LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                            double earnedLeave = ruleExecutor.execute(context);

                            if (earnedLeave > 0) {
                                LeaveBalanceLedger ledgerEntry = LeaveBalanceLedger.builder()
                                        .employee(employee)
                                        .leavePolicy(leavePolicy)
                                        .transactionType(LeaveTransactionType.ACCRUAL)
                                        .amount(earnedLeave)
                                        .transactionDate(month.atEndOfMonth())
                                        .notes("Earned grant for " + month)
                                        .build();
                                leaveBalanceLedgerRepository.save(ledgerEntry);

                                leaveBalanceMetadata.setLastAccrualDate(month.atEndOfMonth());
                                leaveBalanceMetadata.setNextAccrualDate(month.plusMonths(1).atDay(1));
                                leaveBalanceRepository.save(leaveBalanceMetadata);

                                updateSummaryBalance(employee.getEmployeeId(), leavePolicy.getId());
                            }
                        }
                    }
                });
    }
}
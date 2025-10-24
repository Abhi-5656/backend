// REPLACE THIS FILE:
// harshwfm/wfm-backend/HarshWfm-wfm-backend-31884e543d72e5c850158a6e3a92542f90eeca8e/src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/impl/LeaveAccrualServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
// import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet; // <-- This line was removed
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
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveAccrualServiceImpl implements LeaveAccrualService {

    private final EmployeeRepository employeeRepository;
    private final LeaveProfileAssignmentRepository leaveProfileAssignmentRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyRuleExecutor ruleExecutor;
    private final TimesheetRepository timesheetRepository;

    @Override
    @Transactional
    public void accrueRepeatedGrant(YearMonth month) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                    .stream()
                    .findFirst()
                    .ifPresent(assignment -> {
                        // Ensure we do not accrue for months before the assignment is effective.
                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
                            return; // Skip this employee for this month
                        }

                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                        if (leaveProfile != null) {
                            List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
                            for (LeavePolicy leavePolicy : policies) {
                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .facts(new HashMap<>())
                                            .processingMonth(month)
                                            .build();

                                    // FIX: ruleExecutor.execute(context) returns a double
                                    double earnedLeave = ruleExecutor.execute(context);

                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                            .orElse(LeaveBalance.builder()
                                                    .employee(employee)
                                                    .leavePolicy(leavePolicy)
                                                    .balance(0)
                                                    .effectiveDate(assignment.getEffectiveDate()) // Set dates on creation
                                                    .expirationDate(assignment.getExpirationDate()) // Set dates on creation
                                                    .build());

                                    // Ensure dates are updated on existing records
                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                    leaveBalance.setExpirationDate(assignment.getExpirationDate());
                                    leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
                                    leaveBalanceRepository.save(leaveBalance);
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
                    .stream()
                    .findFirst()
                    .ifPresent(assignment -> {
                        // Ensure we do not accrue for months before the assignment is effective.
                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
                            return; // Skip this employee for this month
                        }
                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                        if (leaveProfile != null) {
                            List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
                            for (LeavePolicy leavePolicy : policies) {
                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .facts(new HashMap<>())
                                            .processingMonth(month)
                                            .build();

                                    // FIX: ruleExecutor.execute(context) returns a double
                                    double earnedLeave = ruleExecutor.execute(context);

                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                            .orElse(LeaveBalance.builder()
                                                    .employee(employee)
                                                    .leavePolicy(leavePolicy)
                                                    .balance(0)
                                                    .effectiveDate(assignment.getEffectiveDate()) // Set dates on creation
                                                    .expirationDate(assignment.getExpirationDate()) // Set dates on creation
                                                    .build());

                                    // Ensure dates are updated on existing records
                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                    leaveBalance.setExpirationDate(assignment.getExpirationDate());
                                    leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
                                    leaveBalanceRepository.save(leaveBalance);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * This is the fully corrected recalculation logic.
     */
    @Override
    @Transactional
    public void recalculateTotalLeaveBalance(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Find the employee's active leave assignment
        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream()
                // Find the one that is currently active
                .filter(a -> a.isActive() && a.getEffectiveDate().isBefore(LocalDate.now().plusDays(1)) &&
                        (a.getExpirationDate() == null || a.getExpirationDate().isAfter(LocalDate.now().minusDays(1))))
                .findFirst()
                .ifPresent(assignment -> {
                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                    if (leaveProfile == null) {
                        return;
                    }

                    for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {

                        LocalDate today = LocalDate.now();
                        Optional<LeaveBalance> existingBalanceOpt = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId());

                        // 1. Determine base balance and calculation start
                        double finalBalance = 0;
                        // Default start is the assignment effective date
                        YearMonth calculationStartMonth = YearMonth.from(assignment.getEffectiveDate());
                        boolean manualOverrideActive = false;

                        if (existingBalanceOpt.isPresent()) {
                            LeaveBalance existingBalance = existingBalanceOpt.get();
                            LocalDate expiration = existingBalance.getExpirationDate();

                            // Check if a manual override is active (has a future expiration date)
                            if (expiration != null && (expiration.isAfter(today) || expiration.isEqual(today))) {
                                manualOverrideActive = true;
                                // The manual balance is our starting point
                                finalBalance = existingBalance.getBalance(); // This is the manual balance (e.g., 2.5)
                                // We will start accruing *new* leave from the month *after* the override's effective date
                                calculationStartMonth = YearMonth.from(existingBalance.getEffectiveDate()).plusMonths(1);
                            }
                        }

                        // 2. Run calculation logic (if any)
                        if (leavePolicy.getGrantsConfig() != null) {

                            // Only run accrual calculations for EARNED type.
                            // For FIXED type, the manual override is absolute (finalBalance is already set)
                            if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                                YearMonth endMonth = YearMonth.from(today);

                                // Loop from the correct start month (either assignment or post-override)
                                if (!calculationStartMonth.isAfter(endMonth)) {
                                    for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {

                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .facts(new HashMap<>())
                                                .processingMonth(month)
                                                .build();

                                        // The rule (e.g., EarnedLeaveBalanceRule) will run.
                                        // If Nov punches are full, it returns 1.25. If Dec punches are not, it returns 0.
                                        finalBalance += ruleExecutor.execute(context);
                                    }
                                }

                            } else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
                                // If a manual override is active for a FIXED policy, we don't add anything.
                                // finalBalance is already set to the manual value.
                                if (manualOverrideActive) {
                                    // Do nothing, finalBalance is already the manual value.
                                } else {
                                    // (Logic for calculating FIXED grants from scratch)
                                    FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
                                    if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {
                                        GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
                                        if (grantPeriod == GrantPeriod.YEARLY) {
                                            int startYear = calculationStartMonth.getYear();
                                            int endYear = today.getYear();
                                            for (int year = startYear; year <= endYear; year++) {
                                                LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                        .employee(employee)
                                                        .leavePolicy(leavePolicy)
                                                        .facts(new HashMap<>())
                                                        .processingMonth(YearMonth.of(year, 1))
                                                        .build();
                                                finalBalance += ruleExecutor.execute(context);
                                            }
                                        } else { // Monthly or PayPeriod (simplified to monthly for now)
                                            YearMonth endMonth = YearMonth.now().minusMonths(1); // Accrue up to *last* month
                                            if (!calculationStartMonth.isAfter(endMonth)) {
                                                for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                            .employee(employee)
                                                            .leavePolicy(leavePolicy)
                                                            .facts(new HashMap<>())
                                                            .processingMonth(month)
                                                            .build();
                                                    finalBalance += ruleExecutor.execute(context);
                                                }
                                            }
                                        }
                                    } else { // One-Time grant
                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .facts(new HashMap<>())
                                                .processingMonth(calculationStartMonth)
                                                .build();
                                        finalBalance += ruleExecutor.execute(context);
                                    }
                                }
                            }

                            // 3. Save the final calculated balance
                            LeaveBalance leaveBalance = existingBalanceOpt
                                    .orElse(LeaveBalance.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .build());

                            leaveBalance.setBalance(finalBalance);

                            // If it was a manual override, keep its dates. Otherwise, use assignment dates.
                            if (manualOverrideActive) {
                                leaveBalance.setEffectiveDate(existingBalanceOpt.get().getEffectiveDate());
                                leaveBalance.setExpirationDate(existingBalanceOpt.get().getExpirationDate());
                            } else {
                                leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                leaveBalance.setExpirationDate(assignment.getExpirationDate());
                            }

                            leaveBalanceRepository.save(leaveBalance);
                        }
                    }
                });
    }

    private List<LeavePolicy> getLeavePoliciesFromProfile(LeaveProfile leaveProfile) {
        return leaveProfile.getLeaveProfilePolicies().stream()
                .map(LeaveProfilePolicy::getLeavePolicy)
                .collect(Collectors.toList());
    }
}
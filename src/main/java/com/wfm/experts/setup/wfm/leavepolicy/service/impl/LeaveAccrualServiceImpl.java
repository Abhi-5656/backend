//// REVISED FILE:
//// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/impl/LeaveAccrualServiceImpl.java
//package com.wfm.experts.setup.wfm.leavepolicy.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
//// import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet; // <-- This line was removed
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
//import com.wfm.experts.setup.wfm.leavepolicy.engine.executor.LeavePolicyRuleExecutor;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.FixedGrantConfig;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfilePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
//import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantType;
//import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class LeaveAccrualServiceImpl implements LeaveAccrualService {
//
//    private final EmployeeRepository employeeRepository;
//    private final LeaveProfileAssignmentRepository leaveProfileAssignmentRepository;
//    private final LeaveProfileRepository leaveProfileRepository;
//    private final LeaveBalanceRepository leaveBalanceRepository;
//    private final LeavePolicyRuleExecutor ruleExecutor;
//    private final TimesheetRepository timesheetRepository;
//
//    @Override
//    @Transactional
//    public void accrueRepeatedGrant(YearMonth month) {
//        // This method remains unchanged.
//        List<Employee> employees = employeeRepository.findAll();
//        for (Employee employee : employees) {
//            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
//                    .stream()
//                    .findFirst()
//                    .ifPresent(assignment -> {
//                        // ... (rest of the logic is correct)
//                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
//                            return;
//                        }
//
//                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
//                        if (leaveProfile != null) {
//                            List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
//                            for (LeavePolicy leavePolicy : policies) {
//                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
//                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                            .employee(employee)
//                                            .leavePolicy(leavePolicy)
//                                            .facts(new HashMap<>())
//                                            .processingMonth(month)
//                                            .build();
//
//                                    double earnedLeave = ruleExecutor.execute(context);
//
//                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
//                                            .orElse(LeaveBalance.builder()
//                                                    .employee(employee)
//                                                    .leavePolicy(leavePolicy)
//                                                    .balance(0)
//                                                    .effectiveDate(assignment.getEffectiveDate())
//                                                    .expirationDate(assignment.getExpirationDate())
//                                                    .build());
//
//                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
//                                    leaveBalance.setExpirationDate(assignment.getExpirationDate());
//                                    leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
//                                    leaveBalanceRepository.save(leaveBalance);
//                                }
//                            }
//                        }
//                    });
//        }
//    }
//
//    @Override
//    @Transactional
//    public void accrueEarnedGrant(YearMonth month) {
//        // This method remains unchanged.
//        List<Employee> employees = employeeRepository.findAll();
//        for (Employee employee : employees) {
//            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
//                    .stream()
//                    .findFirst()
//                    .ifPresent(assignment -> {
//                        // ... (rest of the logic is correct)
//                        if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
//                            return;
//                        }
//                        LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
//                        if (leaveProfile != null) {
//                            List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
//                            for (LeavePolicy leavePolicy : policies) {
//                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
//                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                            .employee(employee)
//                                            .leavePolicy(leavePolicy)
//                                            .facts(new HashMap<>())
//                                            .processingMonth(month)
//                                            .build();
//
//                                    double earnedLeave = ruleExecutor.execute(context);
//
//                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
//                                            .orElse(LeaveBalance.builder()
//                                                    .employee(employee)
//                                                    .leavePolicy(leavePolicy)
//                                                    .balance(0)
//                                                    .effectiveDate(assignment.getEffectiveDate())
//                                                    .expirationDate(assignment.getExpirationDate())
//                                                    .build());
//
//                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
//                                    leaveBalance.setExpirationDate(assignment.getExpirationDate());
//                                    leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
//                                    leaveBalanceRepository.save(leaveBalance);
//                                }
//                            }
//                        }
//                    });
//        }
//    }
//
//    /**
//     * This is the "destructive" true-up method. It should ONLY be called
//     * when you need to fix an employee's balance from scratch.
//     * Your punch listener SHOULD NOT call this.
//     */
//    @Override
//    @Transactional
//    public void recalculateTotalLeaveBalance(String employeeId) {
//        Employee employee = employeeRepository.findByEmployeeId(employeeId)
//                .orElseThrow(() -> new RuntimeException("Employee not found"));
//
//        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
//                .stream()
//                .filter(a -> a.isActive() && a.getEffectiveDate().isBefore(LocalDate.now().plusDays(1)) &&
//                        (a.getExpirationDate() == null || a.getExpirationDate().isAfter(LocalDate.now().minusDays(1))))
//                .findFirst()
//                .ifPresent(assignment -> {
//                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
//                    if (leaveProfile == null) {
//                        return;
//                    }
//
//                    for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {
//
//                        LocalDate today = LocalDate.now();
//                        Optional<LeaveBalance> existingBalanceOpt = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId());
//
//                        double finalBalance = 0;
//                        YearMonth calculationStartMonth = YearMonth.from(assignment.getEffectiveDate());
//                        LocalDate newEffectiveDate = assignment.getEffectiveDate();
//                        LocalDate newExpirationDate = assignment.getExpirationDate();
//                        boolean manualOverrideActive = false;
//
//                        if (existingBalanceOpt.isPresent()) {
//                            LeaveBalance existingBalance = existingBalanceOpt.get();
//                            LocalDate expiration = existingBalance.getExpirationDate();
//
//                            // Check if a manual override is active (has a future expiration date)
//                            if (expiration != null && (expiration.isAfter(today) || expiration.isEqual(today))) {
//                                manualOverrideActive = true;
//                                finalBalance = existingBalance.getBalance(); // This is the manual balance (e.g., 2.5)
//                                // We will start calculating *new* leave from the month *after* the override's effective date
//                                calculationStartMonth = YearMonth.from(existingBalance.getEffectiveDate()).plusMonths(1);
//                                newEffectiveDate = existingBalance.getEffectiveDate();
//                                newExpirationDate = existingBalance.getExpirationDate();
//                            }
//                            // Check if a manual override has *expired*
//                            else if (expiration != null && expiration.isBefore(today)) {
//                                finalBalance = 0; // Reset balance to 0
//                                // Start calculating from the month *after* the override expired
//                                calculationStartMonth = YearMonth.from(expiration).plusMonths(1);
//                            }
//                        }
//
//                        // Run calculation loop ONLY for non-manual, or expired-manual
//                        if (!manualOverrideActive && leavePolicy.getGrantsConfig() != null) {
//                            if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
//                                YearMonth endMonth = YearMonth.from(today);
//                                if (!calculationStartMonth.isAfter(endMonth)) {
//                                    for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
//                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                                .employee(employee)
//                                                .leavePolicy(leavePolicy)
//                                                .facts(new HashMap<>())
//                                                .processingMonth(month)
//                                                .build();
//                                        finalBalance += ruleExecutor.execute(context);
//                                    }
//                                }
//                            } else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
//                                // ... (Your existing logic for recalculating fixed grants from scratch)
//                                FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
//                                if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {
//                                    GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
//                                    if (grantPeriod == GrantPeriod.YEARLY) {
//                                        int startYear = calculationStartMonth.getYear();
//                                        int endYear = today.getYear();
//                                        for (int year = startYear; year <= endYear; year++) {
//                                            LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                                    .employee(employee)
//                                                    .leavePolicy(leavePolicy)
//                                                    .facts(new HashMap<>())
//                                                    .processingMonth(YearMonth.of(year, 1))
//                                                    .build();
//                                            finalBalance += ruleExecutor.execute(context);
//                                        }
//                                    } else {
//                                        YearMonth endMonth = YearMonth.now().minusMonths(1);
//                                        if (!calculationStartMonth.isAfter(endMonth)) {
//                                            for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
//                                                LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                                        .employee(employee)
//                                                        .leavePolicy(leavePolicy)
//                                                        .facts(new HashMap<>())
//                                                        .processingMonth(month)
//                                                        .build();
//                                                finalBalance += ruleExecutor.execute(context);
//                                            }
//                                        }
//                                    }
//                                } else { // One-Time grant
//                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                            .employee(employee)
//                                            .leavePolicy(leavePolicy)
//                                            .facts(new HashMap<>())
//                                            .processingMonth(calculationStartMonth)
//                                            .build();
//                                    finalBalance += ruleExecutor.execute(context);
//                                }
//                            }
//                        }
//
//                        // Save the final balance and dates
//                        LeaveBalance leaveBalance = existingBalanceOpt
//                                .orElse(LeaveBalance.builder()
//                                        .employee(employee)
//                                        .leavePolicy(leavePolicy)
//                                        .build());
//
//                        leaveBalance.setBalance(finalBalance);
//                        leaveBalance.setEffectiveDate(newEffectiveDate);
//                        leaveBalance.setExpirationDate(newExpirationDate);
//
//                        leaveBalanceRepository.save(leaveBalance);
//                    }
//                });
//    }
//
//
//    /**
//     * --- THIS IS THE NEW METHOD THAT SOLVES YOUR PROBLEM ---
//     *
//     * This method is "additive". It calculates earned leave for one month
//     * and ADDS it to the existing balance (which could be manual).
//     */
//    @Override
//    @Transactional
//    public void incrementEarnedGrantForMonth(String employeeId, YearMonth month) {
//        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElse(null);
//        if (employee == null) {
//            return; // Employee not found
//        }
//
//        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
//                .stream()
//                .filter(a -> a.isActive()) // Find the active assignment
//                .findFirst()
//                .ifPresent(assignment -> {
//                    // Do not accrue for months before the assignment is effective
//                    if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
//                        return;
//                    }
//
//                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
//                    if (leaveProfile == null) {
//                        return;
//                    }
//
//                    List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
//                    for (LeavePolicy leavePolicy : policies) {
//                        // Only run for EARNED grant types
//                        if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
//
//                            LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
//                                    .employee(employee)
//                                    .leavePolicy(leavePolicy)
//                                    .facts(new HashMap<>())
//                                    .processingMonth(month)
//                                    .build();
//
//                            // Run the rule (e.g., EarnedLeaveBalanceRule)
//                            double earnedLeave = ruleExecutor.execute(context); // This will be 1.0
//
//                            if (earnedLeave > 0) {
//                                // Find the existing balance record, or create a new one
//                                LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
//                                        .orElse(LeaveBalance.builder()
//                                                .employee(employee)
//                                                .leavePolicy(leavePolicy)
//                                                .balance(0)
//                                                .effectiveDate(assignment.getEffectiveDate())
//                                                .expirationDate(assignment.getExpirationDate())
//                                                .build());
//
//                                // **This is Requirement #2:** Add the new balance to the existing one.
//                                // e.g., 2.5 (manual) + 1.0 (earned) = 3.5
//                                leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
//
//                                // If the balance was manual (expirationDate != null), we *clear* the
//                                // expiration date because it is now a *mix* of manual + calculated.
//                                // This makes it a "calculated" balance from now on.
//                                // **This is the key to Requirement #1.**
//                                if (leaveBalance.getExpirationDate() != null) {
//                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate()); // Re-align with assignment
//                                    leaveBalance.setExpirationDate(null); // Clear expiration to mark as "calculated"
//                                }
//
//                                leaveBalanceRepository.save(leaveBalance);
//                            }
//                        }
//                    }
//                });
//    }
//
//    private List<LeavePolicy> getLeavePoliciesFromProfile(LeaveProfile leaveProfile) {
//        return leaveProfile.getLeaveProfilePolicies().stream()
//                .map(LeaveProfilePolicy::getLeavePolicy)
//                .collect(Collectors.toList());
//    }
//}

// FULL FILE:
// harshwfm/wfm-backend/src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/impl/LeaveAccrualServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
// import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet; // This import is not needed here
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository; // This is needed by EarnedLeaveBalanceRule
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

    // This repository is injected but not used directly in this class.
    // It is required by the `EarnedLeaveBalanceRule`, which is part of the `ruleExecutor`.
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
     * Helper to get or create a balance record
     */
    private LeaveBalance getOrCreateBalance(Employee employee, LeavePolicy policy, LocalDate effectiveDate, LocalDate expirationDate) {
        return leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), policy.getId())
                .orElse(LeaveBalance.builder()
                        .employee(employee)
                        .leavePolicy(policy)
                        .currentBalance(0)
                        .totalGranted(0)
                        .usedBalance(0)
                        .status("ACTIVE")
                        .effectiveDate(effectiveDate)
                        .expirationDate(expirationDate)
                        .build());
    }

    // --- UPDATED SCHEDULER METHODS ---

    @Override
    @Transactional
    public void accrueRepeatedGrant(YearMonth month) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                    .stream()
                    .filter(a -> a.isActive()) // Ensure we only use the active assignment
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
                                // Only process FIXED grants (like yearly/monthly grants)
                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {

                                    LeaveBalance leaveBalance = getOrCreateBalance(employee, leavePolicy, assignment.getEffectiveDate(), assignment.getExpirationDate());

                                    // If a manual override is active, the scheduler should NOT add balance.
                                    if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalance.getStatus())) {
                                        continue; // Skip this policy
                                    }

                                    // Idempotency Check: Has this month/year already been accrued?
                                    LocalDate lastAccrual = leaveBalance.getLastAccrualDate();
                                    if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) {
                                        continue; // Already processed this month
                                    }

                                    LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                    double earnedLeave = ruleExecutor.execute(context);

                                    if (earnedLeave > 0) {
                                        // UPDATED LOGIC
                                        leaveBalance.setTotalGranted(leaveBalance.getTotalGranted() + earnedLeave);
                                        leaveBalance.setCurrentBalance(leaveBalance.getCurrentBalance() + earnedLeave);
                                        leaveBalance.setLastAccrualDate(month.atDay(1)); // Mark as accrued
                                        leaveBalance.setNextAccrualDate(month.plusMonths(1).atDay(1)); // TODO: Make this smarter

                                        leaveBalanceRepository.save(leaveBalance);
                                    }
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
                    .filter(a -> a.isActive()) // Ensure we only use the active assignment
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
                                // Only process EARNED grants
                                if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                                    LeaveBalance leaveBalance = getOrCreateBalance(employee, leavePolicy, assignment.getEffectiveDate(), assignment.getExpirationDate());

                                    if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalance.getStatus())) {
                                        continue; // Skip this policy
                                    }

                                    // Idempotency Check
                                    LocalDate lastAccrual = leaveBalance.getLastAccrualDate();
                                    if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) {
                                        continue; // Already processed this month
                                    }

                                    LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                    double earnedLeave = ruleExecutor.execute(context);

                                    if (earnedLeave > 0) {
                                        // UPDATED LOGIC
                                        leaveBalance.setTotalGranted(leaveBalance.getTotalGranted() + earnedLeave);
                                        leaveBalance.setCurrentBalance(leaveBalance.getCurrentBalance() + earnedLeave);
                                        leaveBalance.setLastAccrualDate(month.atEndOfMonth()); // Mark as accrued
                                        leaveBalance.setNextAccrualDate(month.plusMonths(1).atDay(1));

                                        leaveBalanceRepository.save(leaveBalance);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    /**
     * This is the "destructive" true-up method. It is called by the RabbitMQ consumer
     * every time a punch is processed, ensuring the balance is always accurate based on history.
     */
    @Override
    @Transactional
    public void recalculateTotalLeaveBalance(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Find the employee's active leave assignment
        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream()
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
                        LeaveBalance leaveBalance = getOrCreateBalance(employee, leavePolicy, assignment.getEffectiveDate(), assignment.getExpirationDate());

                        // PRESERVE USAGE - this is the most critical part
                        double preservedUsedBalance = leaveBalance.getUsedBalance();

                        // Check for active manual override
                        if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalance.getStatus())) {
                            if (leaveBalance.getExpirationDate() != null && leaveBalance.getExpirationDate().isBefore(today)) {
                                // Override expired. Reset status and proceed with recalculation.
                                leaveBalance.setStatus("ACTIVE");
                            } else {
                                // Manual override is still active. DO NOT recalculate grants.
                                // Just ensure dates are in sync.
                                leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                leaveBalance.setExpirationDate(assignment.getExpirationDate());
                                leaveBalanceRepository.save(leaveBalance);
                                continue; // Skip to the next policy
                            }
                        }

                        // --- RECALCULATE ALL GRANTS FROM SCRATCH ---
                        double newTotalGranted = 0;
                        LocalDate newLastAccrual = null;
                        LocalDate newNextAccrual = null;
                        YearMonth calculationStartMonth = YearMonth.from(assignment.getEffectiveDate());

                        if (leavePolicy.getGrantsConfig() != null) {

                            YearMonth endMonth = YearMonth.from(today); // Calculate up to and including the current month

                            // === EARNED GRANT LOGIC ===
                            if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
                                if (!calculationStartMonth.isAfter(endMonth)) {
                                    for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                        LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                        double grant = ruleExecutor.execute(context);
                                        newTotalGranted += grant;
                                        if (grant > 0) {
                                            newLastAccrual = month.atEndOfMonth();
                                        }
                                    }
                                }
                                // --- FIX: Use withDayOfMonth(1) ---
                                newNextAccrual = today.plusMonths(1).withDayOfMonth(1);
                            }
                            // === FIXED GRANT LOGIC ===
                            else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
                                FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
                                if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {
                                    GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
                                    if (grantPeriod == GrantPeriod.YEARLY) {
                                        int startYear = calculationStartMonth.getYear();
                                        int endYear = today.getYear();
                                        for (int year = startYear; year <= endYear; year++) {
                                            LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, YearMonth.of(year, 1));
                                            double grant = ruleExecutor.execute(context);
                                            newTotalGranted += grant;
                                            if (grant > 0) {
                                                newLastAccrual = LocalDate.of(year, 1, 1);
                                            }
                                        }
                                        newNextAccrual = today.plusYears(1).withDayOfYear(1);
                                    } else { // Monthly or PayPeriod (simplified to monthly)
                                        // Accrue up to *this* month
                                        if (!calculationStartMonth.isAfter(endMonth)) {
                                            for (YearMonth month = calculationStartMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                                LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                                                double grant = ruleExecutor.execute(context);
                                                newTotalGranted += grant;
                                                if(grant > 0) {
                                                    newLastAccrual = month.atDay(1);
                                                }
                                            }
                                        }
                                        // --- FIX: Use withDayOfMonth(1) ---
                                        newNextAccrual = today.plusMonths(1).withDayOfMonth(1);
                                    }
                                } else { // One-Time grant
                                    LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, calculationStartMonth);
                                    newTotalGranted = ruleExecutor.execute(context);
                                    newLastAccrual = assignment.getEffectiveDate();
                                    newNextAccrual = null; // No next accrual for one-time
                                }
                            }
                        } // end grant config check

                        // 3. Save the final calculated balance
                        leaveBalance.setTotalGranted(newTotalGranted);
                        leaveBalance.setUsedBalance(preservedUsedBalance); // Restore the preserved usage
                        leaveBalance.setCurrentBalance(newTotalGranted - preservedUsedBalance); // Recalculate current
                        leaveBalance.setLastAccrualDate(newLastAccrual);
                        leaveBalance.setNextAccrualDate(newNextAccrual);
                        leaveBalance.setStatus("ACTIVE");
                        leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                        leaveBalance.setExpirationDate(assignment.getExpirationDate());

                        leaveBalanceRepository.save(leaveBalance);
                    }
                });
    }


    /**
     * This method is "additive" and "idempotent".
     * It is NOT used by the consumer, but could be used by a more granular scheduler.
     * I have fixed its logic to be correct according to the new architecture.
     */
    @Override
    @Transactional
    public void incrementEarnedGrantForMonth(String employeeId, YearMonth month) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId).orElse(null);
        if (employee == null) {
            return; // Employee not found
        }

        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream()
                .filter(a -> a.isActive()) // Find the active assignment
                .findFirst()
                .ifPresent(assignment -> {
                    // Do not accrue for months before the assignment is effective
                    if (month.isBefore(YearMonth.from(assignment.getEffectiveDate()))) {
                        return;
                    }

                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                    if (leaveProfile == null) {
                        return;
                    }

                    List<LeavePolicy> policies = getLeavePoliciesFromProfile(leaveProfile);
                    for (LeavePolicy leavePolicy : policies) {
                        // Only run for EARNED grant types
                        if (leavePolicy.getGrantsConfig() != null && leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                            LeaveBalance leaveBalance = getOrCreateBalance(employee, leavePolicy, assignment.getEffectiveDate(), assignment.getExpirationDate());

                            // If manually overridden, skip.
                            if ("MANUAL_OVERRIDE".equalsIgnoreCase(leaveBalance.getStatus())) {
                                continue;
                            }

                            // --- IDEMPOTENCY CHECK ---
                            LocalDate lastAccrual = leaveBalance.getLastAccrualDate();
                            if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) {
                                // We have already processed an accrual for this month. Stop.
                                continue;
                            }
                            // ------------------------------------

                            LeavePolicyExecutionContext context = buildContext(employee, leavePolicy, month);
                            double earnedLeave = ruleExecutor.execute(context);

                            if (earnedLeave > 0) {
                                // --- UPDATED LOGIC ---
                                // Add to total_granted and current_balance
                                leaveBalance.setTotalGranted(leaveBalance.getTotalGranted() + earnedLeave);
                                leaveBalance.setCurrentBalance(leaveBalance.getCurrentBalance() + earnedLeave);
                                leaveBalance.setLastAccrualDate(month.atEndOfMonth());
                                leaveBalance.setNextAccrualDate(month.plusMonths(1).atDay(1)); // Simple assumption

                                // Ensure dates align with the assignment.
                                if(leaveBalance.getExpirationDate() == null) {
                                    leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                    leaveBalance.setExpirationDate(assignment.getExpirationDate());
                                }

                                leaveBalanceRepository.save(leaveBalance);
                            }
                        }
                    }
                });
    }
}
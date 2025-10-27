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

                                    // Check if a manual override is active for this policy
                                    Optional<LeaveBalance> existingBalanceOpt = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId());
                                    boolean manualOverrideActive = false;
                                    if(existingBalanceOpt.isPresent()) {
                                        LocalDate expiration = existingBalanceOpt.get().getExpirationDate();
                                        // An active override is one that has an expiration date set
                                        if (expiration != null) {
                                            manualOverrideActive = true;
                                        }
                                    }

                                    // If a manual override is active, the scheduler should NOT add balance.
                                    if (manualOverrideActive) {
                                        continue; // Skip this policy, let recalculate handle it
                                    }

                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .facts(new HashMap<>())
                                            .processingMonth(month)
                                            .build();

                                    double earnedLeave = ruleExecutor.execute(context);

                                    if (earnedLeave > 0) {
                                        LeaveBalance leaveBalance = existingBalanceOpt
                                                .orElse(LeaveBalance.builder()
                                                        .employee(employee)
                                                        .leavePolicy(leavePolicy)
                                                        .balance(0)
                                                        .effectiveDate(assignment.getEffectiveDate()) // Set dates on creation
                                                        .expirationDate(assignment.getExpirationDate()) // Set dates on creation
                                                        .build());

                                        // Ensure dates match assignment (since this is not a manual override)
                                        leaveBalance.setEffectiveDate(assignment.getEffectiveDate());
                                        leaveBalance.setExpirationDate(assignment.getExpirationDate());
                                        leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);
                                        leaveBalanceRepository.save(leaveBalance);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    /**
     * THIS METHOD IS NOW DISABLED / COMMENTED OUT in LeaveAccrualScheduler
     * It is kept here in case you need it for a different purpose later.
     * The `incrementEarnedGrantForMonth` method is used by the consumer instead.
     */
    @Override
    @Transactional
    public void accrueEarnedGrant(YearMonth month) {
        // List<Employee> employees = employeeRepository.findAll();
        // for (Employee employee : employees) {
        //     leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
        //             .stream()
        //             .filter(a -> a.isActive()) // Ensure we only use the active assignment
        //             .findFirst()
        //             .ifPresent(assignment -> {
        //                 // ... (logic as before)
        //             });
        // }
    }

    /**
     * This is the "destructive" true-up method. It should ONLY be called
     * when you need to fix an employee's balance from scratch.
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
                        Optional<LeaveBalance> existingBalanceOpt = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId());

                        double finalBalance = 0;
                        YearMonth calculationStartMonth = YearMonth.from(assignment.getEffectiveDate());
                        LocalDate newEffectiveDate = assignment.getEffectiveDate();
                        LocalDate newExpirationDate = assignment.getExpirationDate();

                        boolean manualOverrideActive = false; // Flag to skip calculation loops

                        if (existingBalanceOpt.isPresent()) {
                            LeaveBalance existingBalance = existingBalanceOpt.get();
                            LocalDate expiration = existingBalance.getExpirationDate();

                            // SCENARIO B: Active Manual Override.
                            if (expiration != null && (expiration.isAfter(today) || expiration.isEqual(today))) {
                                manualOverrideActive = true;
                                finalBalance = existingBalance.getBalance();
                                calculationStartMonth = null; // Set flag to SKIP calculation
                                newEffectiveDate = existingBalance.getEffectiveDate();
                                newExpirationDate = existingBalance.getExpirationDate();
                            }
                            // SCENARIO C: Expired Manual Override.
                            else if (expiration != null && expiration.isBefore(today)) {
                                finalBalance = 0; // Reset balance
                                // Start calculating from the month AFTER the override expired
                                calculationStartMonth = YearMonth.from(expiration).plusMonths(1);
                            }
                        }

                        // Run calculation loop ONLY if it's not an active manual override
                        if (!manualOverrideActive && leavePolicy.getGrantsConfig() != null) {

                            // === EARNED GRANT LOGIC ===
                            if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
                                YearMonth endMonth = YearMonth.from(today);
                                if (!calculationStartMonth.isAfter(endMonth)) {
                                    // Loop from the correct start month to the current month
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
                            // === FIXED GRANT LOGIC ===
                            else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
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
                                    } else { // Monthly or PayPeriod (simplified to monthly)
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
                        } // end calculation logic

                        // 3. Save the final balance and dates
                        LeaveBalance leaveBalance = existingBalanceOpt
                                .orElse(LeaveBalance.builder()
                                        .employee(employee)
                                        .leavePolicy(leavePolicy)
                                        .build());

                        leaveBalance.setBalance(finalBalance);
                        leaveBalance.setEffectiveDate(newEffectiveDate);
                        leaveBalance.setExpirationDate(newExpirationDate);

                        leaveBalanceRepository.save(leaveBalance);
                    }
                });
    }


    /**
     * --- THIS IS THE NEW METHOD THAT SOLVES YOUR PROBLEM ---
     *
     * This method is "additive" and "idempotent". It calculates earned leave for one month
     * and ADDS it to the existing balance (which could be manual).
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

                            // Find the existing balance record, or create a new one
                            LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                    .orElse(LeaveBalance.builder()
                                            .employee(employee)
                                            .leavePolicy(leavePolicy)
                                            .balance(0)
                                            .effectiveDate(assignment.getEffectiveDate())
                                            .expirationDate(assignment.getExpirationDate())
                                            .build());

                            // --- IDEMPOTENCY CHECK (THE FIX) ---
                            LocalDate lastAccrual = leaveBalance.getLastAccrualDate();
                            if (lastAccrual != null && YearMonth.from(lastAccrual).equals(month)) {
                                // We have already processed an accrual for this month. Stop.
                                return;
                            }
                            // ------------------------------------

                            LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                    .employee(employee)
                                    .leavePolicy(leavePolicy)
                                    .facts(new HashMap<>())
                                    .processingMonth(month)
                                    .build();

                            // Run the rule (e.g., EarnedLeaveBalanceRule, which now checks Timesheet status)
                            double earnedLeave = ruleExecutor.execute(context); // This will be 1.25

                            if (earnedLeave > 0) {
                                // Add the new balance to the existing one.
                                // e.g., 0 (from reset) + 1.25 (earned) = 1.25
                                leaveBalance.setBalance(leaveBalance.getBalance() + earnedLeave);

                                // **IMPORTANT:** Mark this month as processed by
                                // setting the last accrual date to the end of the processed month.
                                leaveBalance.setLastAccrualDate(month.atEndOfMonth());

                                // If this was a *new* balance, or a *non-manual* balance,
                                // ensure its dates align with the assignment.
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

    private List<LeavePolicy> getLeavePoliciesFromProfile(LeaveProfile leaveProfile) {
        return leaveProfile.getLeaveProfilePolicies().stream()
                .map(LeaveProfilePolicy::getLeavePolicy)
                .collect(Collectors.toList());
    }
}
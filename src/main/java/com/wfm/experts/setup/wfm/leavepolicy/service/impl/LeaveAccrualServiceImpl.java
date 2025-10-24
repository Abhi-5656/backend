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

    @Override
    @Transactional
    public void recalculateTotalLeaveBalance(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        leaveProfileAssignmentRepository.findByEmployeeId(employee.getEmployeeId())
                .stream()
                .findFirst()
                .ifPresent(assignment -> {
                    LeaveProfile leaveProfile = leaveProfileRepository.findById(assignment.getLeaveProfileId()).orElse(null);
                    if (leaveProfile != null) {
                        for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {

                            // --- THIS IS THE CRITICAL LOGIC ---
                            // 1. Check for an existing balance record
                            Optional<LeaveBalance> existingBalanceOpt = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId());
                            LocalDate today = LocalDate.now();
                            boolean isActiveManualOverride = false;

                            LocalDate calculationStartDate = assignment.getEffectiveDate(); // Default: start from assignment
                            double baseBalance = 0; // Default: start from 0

                            if (existingBalanceOpt.isPresent()) {
                                LeaveBalance existingBalance = existingBalanceOpt.get();
                                LocalDate expiration = existingBalance.getExpirationDate();

                                // An active manual override has an expiration date set in the future.
                                if (expiration != null && (expiration.isAfter(today) || expiration.isEqual(today))) {
                                    isActiveManualOverride = true;
                                    // This is our "stake in the ground"
                                    calculationStartDate = existingBalance.getEffectiveDate();
                                    baseBalance = existingBalance.getBalance(); // baseBalance is set to 2.0
                                }
                                // If expiration is null or in the past, it's NOT an active override.
                                // We proceed with recalculation from the assignment date.
                            }
                            // --- END OF CRITICAL LOGIC ---


                            if (leavePolicy.getGrantsConfig() != null) {
                                double calculatedBalance = 0; // This will hold the *newly* calculated amount (1.25)

                                if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {

                                    YearMonth startMonth;
                                    if (isActiveManualOverride) {
                                        // MANUAL OVERRIDE PATH
                                        // We start calculating *new* leave from the month *of* the manual balance's effective date.
                                        startMonth = YearMonth.from(calculationStartDate);
                                    } else {
                                        // STANDARD PATH
                                        // Start from the beginning of the assignment.
                                        startMonth = YearMonth.from(calculationStartDate);
                                    }

                                    // --- Common Calculation Logic ---
                                    LocalDate endDate = LocalDate.now();
                                    YearMonth endMonth = YearMonth.from(endDate); // Recalculate up to/including the current month

                                    if (!startMonth.isAfter(endMonth)) {
                                        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                            LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                    .employee(employee)
                                                    .leavePolicy(leavePolicy)
                                                    .facts(new HashMap<>())
                                                    .processingMonth(month)
                                                    .build();
                                            // ruleExecutor.execute(context) will call EarnedLeaveBalanceRule
                                            calculatedBalance += ruleExecutor.execute(context); // This will be 1.25
                                        }
                                    }

                                } else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {

                                    if (isActiveManualOverride) {
                                        // For FIXED grants, a manual override is absolute. We do not add to it.
                                        continue; // Skip this policy
                                    }

                                    // --- Standard FIXED Recalculation (if not manual override) ---
                                    FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
                                    if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {
                                        GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();
                                        if (grantPeriod == GrantPeriod.YEARLY) {
                                            int startYear = assignment.getEffectiveDate().getYear();
                                            int endYear = LocalDate.now().getYear();
                                            for (int year = startYear; year <= endYear; year++) {
                                                LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                        .employee(employee)
                                                        .leavePolicy(leavePolicy)
                                                        .facts(new HashMap<>())
                                                        .processingMonth(YearMonth.of(year, 1))
                                                        .build();
                                                calculatedBalance += ruleExecutor.execute(context);
                                            }
                                        } else {
                                            YearMonth startMonth = YearMonth.from(assignment.getEffectiveDate());
                                            YearMonth endMonth = YearMonth.now().minusMonths(1);
                                            if (!startMonth.isAfter(endMonth)) {
                                                for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                                    LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                            .employee(employee)
                                                            .leavePolicy(leavePolicy)
                                                            .facts(new HashMap<>())
                                                            .processingMonth(month)
                                                            .build();
                                                    calculatedBalance += ruleExecutor.execute(context);
                                                }
                                            }
                                        }
                                    } else {
                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .facts(new HashMap<>())
                                                .processingMonth(YearMonth.from(assignment.getEffectiveDate()))
                                                .build();
                                        calculatedBalance += ruleExecutor.execute(context);
                                    }
                                }

                                // --- Save Logic ---
                                LeaveBalance leaveBalance = existingBalanceOpt
                                        .orElse(LeaveBalance.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .build());

                                // The new total balance is the base (0 or manual) + new calculations
                                leaveBalance.setBalance(baseBalance + calculatedBalance); // This will be 2.0 + 1.25

                                if (isActiveManualOverride) {
                                    // If it was a manual override, we keep its dates
                                    leaveBalance.setEffectiveDate(existingBalanceOpt.get().getEffectiveDate());
                                    leaveBalance.setExpirationDate(existingBalanceOpt.get().getExpirationDate());
                                } else {
                                    // It's a system-calculated balance, so it should align with the assignment
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

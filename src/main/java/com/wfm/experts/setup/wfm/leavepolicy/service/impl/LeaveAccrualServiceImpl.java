package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
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

                                    double earnedLeave = ruleExecutor.execute(context);

                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                            .orElse(LeaveBalance.builder()
                                                    .employee(employee)
                                                    .leavePolicy(leavePolicy)
                                                    .balance(0)
                                                    .build());

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

                                    double earnedLeave = ruleExecutor.execute(context);

                                    LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                            .orElse(LeaveBalance.builder()
                                                    .employee(employee)
                                                    .leavePolicy(leavePolicy)
                                                    .balance(0)
                                                    .build());

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
                            if (leavePolicy.getGrantsConfig() != null) {
                                double totalBalance = 0;
                                if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.EARNED) {
                                    LocalDate startDate = assignment.getEffectiveDate();
                                    LocalDate endDate = LocalDate.now();
                                    YearMonth startMonth = YearMonth.from(startDate);
                                    YearMonth endMonth = YearMonth.from(endDate);

                                    for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .facts(new HashMap<>())
                                                .processingMonth(month)
                                                .build();
                                        totalBalance += ruleExecutor.execute(context);
                                    }
                                } else if (leavePolicy.getGrantsConfig().getGrantType() == GrantType.FIXED) {
                                    FixedGrantConfig fixedGrant = leavePolicy.getGrantsConfig().getFixedGrant();
                                    if (fixedGrant != null && fixedGrant.getFrequency() == GrantFrequency.REPEATEDLY) {

                                        GrantPeriod grantPeriod = fixedGrant.getRepeatedlyDetails().getGrantPeriod();

                                        if (grantPeriod == GrantPeriod.YEARLY) {
                                            // Loop through years for yearly grants
                                            int startYear = assignment.getEffectiveDate().getYear();
                                            int endYear = LocalDate.now().getYear();
                                            for (int year = startYear; year <= endYear; year++) {
                                                LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                        .employee(employee)
                                                        .leavePolicy(leavePolicy)
                                                        .facts(new HashMap<>())
                                                        .processingMonth(YearMonth.of(year, 1)) // Use any month of the year
                                                        .build();
                                                totalBalance += ruleExecutor.execute(context);
                                            }
                                        } else { // Handles MONTHLY and PAY_PERIOD
                                            // Original monthly loop is correct for these cases
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
                                                    totalBalance += ruleExecutor.execute(context);
                                                }
                                            }
                                        }

                                    } else { // This handles ONE_TIME fixed grants correctly.
                                        LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .facts(new HashMap<>())
                                                .processingMonth(YearMonth.from(assignment.getEffectiveDate()))
                                                .build();
                                        totalBalance += ruleExecutor.execute(context);
                                    }
                                }

                                LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                                        .orElse(LeaveBalance.builder()
                                                .employee(employee)
                                                .leavePolicy(leavePolicy)
                                                .build());

                                leaveBalance.setBalance(totalBalance);
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
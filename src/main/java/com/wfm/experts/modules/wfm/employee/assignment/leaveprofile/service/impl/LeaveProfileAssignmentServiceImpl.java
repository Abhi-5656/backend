// Modify the file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/service/impl/LeaveProfileAssignmentServiceImpl.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.engine.executor.LeavePolicyRuleExecutor;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveProfileAssignmentServiceImpl implements LeaveProfileAssignmentService {

    private final LeaveProfileAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveProfileAssignmentMapper mapper;
    private final LeavePolicyRuleExecutor ruleExecutor;

    @Override
    public List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto) {
        LeaveProfile leaveProfile = leaveProfileRepository.findById(dto.getLeaveProfileId())
                .orElseThrow(() -> new RuntimeException("LeaveProfile not found with id: " + dto.getLeaveProfileId()));

        List<LeaveProfileAssignment> assignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

            LeaveProfileAssignment assignment = LeaveProfileAssignment.builder()
                    .employeeId(employeeId)
                    .leaveProfileId(dto.getLeaveProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            assignments.add(assignment);

            for (LeavePolicy leavePolicy : getLeavePoliciesFromProfile(leaveProfile)) {
                LeavePolicyExecutionContext context = LeavePolicyExecutionContext.builder()
                        .employee(employee)
                        .leavePolicy(leavePolicy)
                        .facts(new HashMap<>())
                        .build();

                double initialBalance = ruleExecutor.execute(context);
                LeaveBalance leaveBalance = LeaveBalance.builder()
                        .employee(employee)
                        .leavePolicy(leavePolicy)
                        .balance(initialBalance)
                        .build();
                leaveBalanceRepository.save(leaveBalance);
            }
        }

        List<LeaveProfileAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        return savedAssignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private List<LeavePolicy> getLeavePoliciesFromProfile(LeaveProfile leaveProfile) {
        return leaveProfile.getLeaveProfilePolicies().stream()
                .map(lpp -> lpp.getLeavePolicy())
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveProfileAssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
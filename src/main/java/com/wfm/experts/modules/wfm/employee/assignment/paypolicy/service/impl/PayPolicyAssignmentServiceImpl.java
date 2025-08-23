package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.impl;

import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.mapper.PayPolicyAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository.PayPolicyAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayPolicyAssignmentServiceImpl implements PayPolicyAssignmentService {

    private final PayPolicyAssignmentRepository payPolicyAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PayPolicyAssignmentMapper payPolicyAssignmentMapper;

    @Override
    public List<PayPolicyAssignmentDTO> assignPayPolicy(PayPolicyAssignmentDTO dto) {
        List<String> employeeIds = dto.getEmployeeIds();

        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException("employeeIds list cannot be null or empty.");
        }

        List<PayPolicyAssignmentDTO> result = employeeIds.stream().map(employeeId -> {
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
            if (employeeOpt.isEmpty()) {
                throw new IllegalArgumentException("Employee not found for id: " + employeeId);
            }

            PayPolicyAssignment entity = PayPolicyAssignment.builder()
                    .employeeId(employeeId)
                    .payPolicyId(dto.getPayPolicyId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            PayPolicyAssignment saved = payPolicyAssignmentRepository.save(entity);
            return payPolicyAssignmentMapper.toDTO(saved);
        }).collect(Collectors.toList());

        return result;
    }

    @Override
    public List<PayPolicyAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        List<PayPolicyAssignment> assignments = payPolicyAssignmentRepository.findByEmployeeId(employeeId);
        return assignments.stream()
                .map(payPolicyAssignmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PayPolicyAssignmentDTO getCurrentAssignment(String employeeId) {
        LocalDate today = LocalDate.now();
        Optional<PayPolicyAssignment> assignmentOpt =
                payPolicyAssignmentRepository.findByEmployeeIdAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqual(
                        employeeId, today, today);
        return assignmentOpt.map(payPolicyAssignmentMapper::toDTO)
                .orElse(null);
    }


    @Override
    public List<PayPolicyAssignmentDTO> getAllAssignments() {
        return payPolicyAssignmentRepository.findAll().stream()
                .map(payPolicyAssignmentMapper::toDTO)
                .collect(Collectors.toList());
    }
}

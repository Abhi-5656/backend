package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.impl;

import com.wfm.experts.entity.tenant.common.Employee;
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
    public PayPolicyAssignmentDTO assignPayPolicy(PayPolicyAssignmentDTO dto) {
        // Check if employee exists, throw if not
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(dto.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found for id: " + dto.getEmployeeId());
        }

        // Prepare entity
        PayPolicyAssignment assignment = payPolicyAssignmentMapper.toEntity(dto);

        // Set assignment time if not provided
        if (assignment.getAssignedAt() == null) {
            assignment.setAssignedAt(LocalDateTime.now());
        }
        // Mark as active
        assignment.setActive(true);

        // Save
        PayPolicyAssignment saved = payPolicyAssignmentRepository.save(assignment);
        return payPolicyAssignmentMapper.toDTO(saved);
    }

    @Override
    public List<PayPolicyAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        List<PayPolicyAssignment> assignments = payPolicyAssignmentRepository.findByEmployeeId(employeeId);
        return assignments.stream()
                .map(payPolicyAssignmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PayPolicyAssignmentDTO getCurrentAssignment(String employeeId, LocalDate effectiveDate, LocalDate expirationDate) {
        Optional<PayPolicyAssignment> assignmentOpt =
                payPolicyAssignmentRepository.findByEmployeeIdAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqual(
                        employeeId, effectiveDate, expirationDate);
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

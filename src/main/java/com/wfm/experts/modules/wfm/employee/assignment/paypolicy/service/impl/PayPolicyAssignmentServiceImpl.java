package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.impl;

// Import the exceptions
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception.DuplicatePayPolicyAssignmentException;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception.PayPolicyAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception.PayPolicyAssignmentValidationException;

import com.wfm.experts.setup.wfm.paypolicy.repository.PayPolicyRepository;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.mapper.PayPolicyAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository.PayPolicyAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.service.PayPolicyAssignmentService;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
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
    private final PayPolicyRepository payPolicyRepository; // Injected
    private final PayPolicyAssignmentMapper payPolicyAssignmentMapper;

    @Override
    public List<PayPolicyAssignmentDTO> assignPayPolicy(PayPolicyAssignmentDTO dto) {
        List<String> employeeIds = dto.getEmployeeIds();

        // 1. Validate Expiration Date
        if (dto.getExpirationDate() != null && dto.getExpirationDate().isBefore(dto.getEffectiveDate())) {
            throw new PayPolicyAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        // 2. Validate Pay Policy exists
        if (!payPolicyRepository.existsById(dto.getPayPolicyId())) {
            throw new PayPolicyAssignmentResourceNotFoundException("PayPolicy not found with id: " + dto.getPayPolicyId());
        }

        List<PayPolicyAssignmentDTO> result = employeeIds.stream().map(employeeId -> {
            // 3. Validate Employee exists
            if (!employeeRepository.existsByEmployeeId(employeeId)) {
                throw new PayPolicyAssignmentResourceNotFoundException("Employee not found for id: " + employeeId);
            }

            // --- NEW LOGIC: Supersede existing active assignments ---

            // 4. Find all existing assignments for the employee
            List<PayPolicyAssignment> existingAssignments = payPolicyAssignmentRepository.findByEmployeeId(employeeId);

            // 5. Find and deactivate any *currently active* assignments
            List<PayPolicyAssignment> assignmentsToDeactivate = existingAssignments.stream()
                    .filter(PayPolicyAssignment::isActive) // Find only active assignments
                    .peek(existing -> {
                        // Set expiration to the day before the new one starts
                        existing.setExpirationDate(dto.getEffectiveDate().minusDays(1));
                        existing.setActive(false);
                    })
                    .collect(Collectors.toList());

            // 6. Save the updated (now inactive) assignments
            if (!assignmentsToDeactivate.isEmpty()) {
                payPolicyAssignmentRepository.saveAll(assignmentsToDeactivate);
            }

            // 7. Check for conflicts with *any* assignment (active or inactive)
            // This prevents creating an assignment that starts *before* an already expired one ends
            LocalDate newEndDate = (dto.getExpirationDate() != null) ? dto.getExpirationDate() : LocalDate.of(9999, 12, 31);
            boolean hasOverlapWithHistory = existingAssignments.stream()
                    .filter(a -> !assignmentsToDeactivate.contains(a)) // Don't check against the ones we just deactivated
                    .anyMatch(a -> {
                        LocalDate existingEndDate = (a.getExpirationDate() != null) ? a.getExpirationDate() : LocalDate.of(9999, 12, 31);
                        // Check if new period (A) overlaps with existing period (B)
                        // (A_start <= B_end) and (A_end >= B_start)
                        return (dto.getEffectiveDate().isBefore(existingEndDate) || dto.getEffectiveDate().isEqual(existingEndDate)) &&
                                (newEndDate.isAfter(a.getEffectiveDate()) || newEndDate.isEqual(a.getEffectiveDate()));
                    });

            if (hasOverlapWithHistory) {
                throw new DuplicatePayPolicyAssignmentException("The new assignment dates overlap with an existing historical assignment for employee " + employeeId);
            }

            // 8. Create the new assignment
            PayPolicyAssignment entity = PayPolicyAssignment.builder()
                    .employeeId(employeeId)
                    .payPolicyId(dto.getPayPolicyId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true) // The new assignment is now the active one
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
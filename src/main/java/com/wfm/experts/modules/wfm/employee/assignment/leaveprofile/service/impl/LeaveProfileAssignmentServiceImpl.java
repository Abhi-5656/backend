package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception.DuplicateLeaveProfileAssignmentException;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception.LeaveProfileAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception.LeaveProfileAssignmentValidationException;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveProfileAssignmentServiceImpl implements LeaveProfileAssignmentService {

    private final LeaveProfileAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveProfileAssignmentMapper mapper;
    private final LeaveAccrualService leaveAccrualService;

    // Define a PostgreSQL-compatible "far future" date
    private static final LocalDate POSTGRES_MAX_DATE = LocalDate.of(9999, 12, 31);

    @Override
    public List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto) {
        // 1. Validate Expiration Date
        if (dto.getExpirationDate() != null && dto.getExpirationDate().isBefore(dto.getEffectiveDate())) {
            throw new LeaveProfileAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        // 2. Validate Leave Profile exists
        leaveProfileRepository.findById(dto.getLeaveProfileId())
                .orElseThrow(() -> new LeaveProfileAssignmentResourceNotFoundException("LeaveProfile not found with id: " + dto.getLeaveProfileId()));

        List<LeaveProfileAssignment> savedAssignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            // 3. Validate Employee exists
            employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new LeaveProfileAssignmentResourceNotFoundException("Employee not found with id: " + employeeId));

            // 4. Check for conflicts with *any* assignment
            LocalDate endDate = (dto.getExpirationDate() != null) ? dto.getExpirationDate() : POSTGRES_MAX_DATE;
            if (assignmentRepository.existsOverlappingAssignment(employeeId, dto.getEffectiveDate(), endDate)) {
                throw new DuplicateLeaveProfileAssignmentException("An assignment for employee " + employeeId + " already exists within the specified date range.");
            }

            // 5. Deactivate all existing assignments for this employee.
            List<LeaveProfileAssignment> existingAssignments = assignmentRepository.findByEmployeeId(employeeId);
            for (LeaveProfileAssignment existing : existingAssignments) {
                if (existing.isActive()) {
                    existing.setActive(false);
                    // Optionally set expiration on the *last active* record
                    if (existing.getExpirationDate() == null) {
                        existing.setExpirationDate(dto.getEffectiveDate().minusDays(1));
                    }
                    assignmentRepository.save(existing);
                }
            }

            // 6. Create the new, active assignment.
            LeaveProfileAssignment newAssignment = LeaveProfileAssignment.builder()
                    .employeeId(employeeId)
                    .leaveProfileId(dto.getLeaveProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            savedAssignments.add(assignmentRepository.save(newAssignment));

            // 7. Trigger a full recalculation
            leaveAccrualService.recalculateTotalLeaveBalance(employeeId);
        }

        return savedAssignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new LeaveProfileAssignmentResourceNotFoundException("Employee not found with id: "+ employeeId);
        }
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

    @Override
    public LeaveProfileAssignmentDTO expireAssignment(Long id, LocalDate expirationDate) {
        LeaveProfileAssignment entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileAssignmentResourceNotFoundException(
                        "LeaveProfileAssignment not found for id: " + id));

        if (expirationDate.isBefore(entity.getEffectiveDate())) {
            throw new LeaveProfileAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        entity.setExpirationDate(expirationDate);
        entity.setActive(false); // Expiring an assignment should also deactivate it
        LeaveProfileAssignment saved = assignmentRepository.save(entity);

        // Trigger recalculation after expiration
        leaveAccrualService.recalculateTotalLeaveBalance(entity.getEmployeeId());

        return mapper.toDto(saved);
    }

    @Override
    public void deactivateAssignment(Long id) {
        LeaveProfileAssignment entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileAssignmentResourceNotFoundException(
                        "LeaveProfileAssignment not found for id: " + id));

        entity.setActive(false);
        assignmentRepository.save(entity);

        // Trigger recalculation after deactivation
        leaveAccrualService.recalculateTotalLeaveBalance(entity.getEmployeeId());
    }
}
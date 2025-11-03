package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.MultiShiftRotationAssignmentRequestDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
// Import new exceptions
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception.DuplicateShiftRotationAssignmentException;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception.ShiftRotationAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.exception.ShiftRotationAssignmentValidationException;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.mapper.ShiftRotationAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.repository.ShiftRotationAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.ShiftRotationAssignmentService;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRotationRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftRotationAssignmentServiceImpl implements ShiftRotationAssignmentService {

    private final ShiftRotationAssignmentRepository assignmentRepository;
    private final ShiftRotationRepository shiftRotationRepository;
    private final ShiftRotationAssignmentMapper mapper;
    private final EmployeeShiftService employeeShiftService;
    private final EmployeeRepository employeeRepository;

    // Define a PostgreSQL-compatible "far future" date
    private static final LocalDate POSTGRES_MAX_DATE = LocalDate.of(9999, 12, 31);


    @Override
    public ShiftRotationAssignment updateAssignment(Long id, ShiftRotationAssignmentDTO dto) {
        ShiftRotationAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new ShiftRotationAssignmentResourceNotFoundException("Assignment not found with id: " + id));

        ShiftRotation rotation = shiftRotationRepository.findById(dto.getShiftRotationId())
                .orElseThrow(() -> new ShiftRotationAssignmentResourceNotFoundException("Invalid ShiftRotation ID: " + dto.getShiftRotationId()));

        ShiftRotationAssignment updated = mapper.toEntity(dto);
        updated.setId(id);
        updated.setShiftRotation(rotation);

        return assignmentRepository.save(updated);
    }

    @Override
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ShiftRotationAssignmentResourceNotFoundException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    @Override
    public ShiftRotationAssignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ShiftRotationAssignmentResourceNotFoundException("Assignment not found with id: " + id));
    }

    @Override
    public List<ShiftRotationAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    @Transactional
    public List<ShiftRotationAssignment> assignShiftRotationToMultipleEmployees(MultiShiftRotationAssignmentRequestDTO requestDTO) {
        // 1. Validate Expiration Date
        if (requestDTO.getExpirationDate() != null && requestDTO.getExpirationDate().isBefore(requestDTO.getEffectiveDate())) {
            throw new ShiftRotationAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        // 2. Fetch ShiftRotation entity once
        ShiftRotation shiftRotation = shiftRotationRepository.findById(requestDTO.getShiftRotationId())
                .orElseThrow(() -> new ShiftRotationAssignmentResourceNotFoundException("ShiftRotation not found with id: " + requestDTO.getShiftRotationId()));

        List<String> employeeIds = requestDTO.getEmployees();

        // 3. Validate all employees exist
        List<String> missingEmployeeIds = employeeIds.stream()
                .filter(eid -> !employeeRepository.existsByEmployeeId(eid))
                .toList();
        if (!missingEmployeeIds.isEmpty()) {
            throw new ShiftRotationAssignmentResourceNotFoundException("Employee not found with id:" + String.join(", ", missingEmployeeIds));
        }

        // 4. Check for overlaps for all employees *before* creating any
        LocalDate endDate = (requestDTO.getExpirationDate() != null) ? requestDTO.getExpirationDate() : POSTGRES_MAX_DATE;
        for (String employeeId : employeeIds) {
            if (assignmentRepository.existsOverlappingAssignment(employeeId, requestDTO.getEffectiveDate(), endDate)) {
                throw new DuplicateShiftRotationAssignmentException("An assignment for employee " + employeeId + " already exists within the specified date range.");
            }
        }

        // 5. Build all assignment entities
        List<ShiftRotationAssignment> assignments = employeeIds.stream()
                .map(employeeId -> ShiftRotationAssignment.builder()
                        .employeeId(employeeId)
                        .shiftRotation(shiftRotation)
                        .effectiveDate(requestDTO.getEffectiveDate())
                        .expirationDate(requestDTO.getExpirationDate())
                        .build())
                .toList();

        // 6. Bulk insert
        List<ShiftRotationAssignment> savedAssignments = assignmentRepository.saveAll(assignments);

        // 7. Determine roster window
        LocalDate from = requestDTO.getEffectiveDate();
        LocalDate to = requestDTO.getExpirationDate() != null
                ? requestDTO.getExpirationDate()
                : from.plusYears(1); // Default to 1 year if no expiration is set for roster generation

        // 8. Generate shifts for ALL employees in this batch
        employeeShiftService.generateShiftsFromRotation(employeeIds, from, to);

        return savedAssignments;
    }

}
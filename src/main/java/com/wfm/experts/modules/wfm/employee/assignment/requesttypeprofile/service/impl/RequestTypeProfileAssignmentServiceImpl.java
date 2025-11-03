package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto.RequestTypeProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.entity.RequestTypeProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception.DuplicateRequestTypeProfileAssignmentException;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception.RequestTypeProfileAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.exception.RequestTypeProfileAssignmentValidationException;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.mapper.RequestTypeProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.repository.RequestTypeProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service.RequestTypeProfileAssignmentService;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeProfileRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestTypeProfileAssignmentServiceImpl implements RequestTypeProfileAssignmentService {

    private final RequestTypeProfileAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final RequestTypeProfileRepository requestTypeProfileRepository;
    private final RequestTypeProfileAssignmentMapper mapper;

    // Define a PostgreSQL-compatible "far future" date
    private static final LocalDate POSTGRES_MAX_DATE = LocalDate.of(9999, 12, 31);

    @Override
    public List<RequestTypeProfileAssignmentDTO> assignRequestTypeProfile(RequestTypeProfileAssignmentDTO dto) {

        // 1. Validate Expiration Date
        if (dto.getExpirationDate() != null && dto.getExpirationDate().isBefore(dto.getEffectiveDate())) {
            throw new RequestTypeProfileAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        // 2. Validate Request Type Profile exists
        requestTypeProfileRepository.findById(dto.getRequestTypeProfileId())
                .orElseThrow(() -> new RequestTypeProfileAssignmentResourceNotFoundException("RequestTypeProfile not found with id: " + dto.getRequestTypeProfileId()));

        List<RequestTypeProfileAssignment> assignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            // 3. Validate Employee exists
            employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RequestTypeProfileAssignmentResourceNotFoundException("Employee not found with id: " + employeeId));

            // 4. Check for overlapping assignments
            LocalDate endDate = (dto.getExpirationDate() != null) ? dto.getExpirationDate() : POSTGRES_MAX_DATE;
            if (assignmentRepository.existsOverlappingAssignment(employeeId, dto.getEffectiveDate(), endDate)) {
                throw new DuplicateRequestTypeProfileAssignmentException("An assignment for employee " + employeeId + " already exists within the specified date range.");
            }

            // 5. Deactivate existing active assignments
            assignmentRepository.findByEmployeeId(employeeId).stream()
                    .filter(RequestTypeProfileAssignment::isActive)
                    .forEach(a -> {
                        a.setActive(false);
                        assignmentRepository.save(a);
                    });

            // 6. Create the new assignment
            RequestTypeProfileAssignment assignment = RequestTypeProfileAssignment.builder()
                    .employeeId(employeeId)
                    .requestTypeProfileId(dto.getRequestTypeProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            assignments.add(assignmentRepository.save(assignment)); // Save and add to list
        }

        // Return DTOs of the newly created assignments
        return assignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestTypeProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        // 4. Validate Employee exists before trying to find assignments
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new RequestTypeProfileAssignmentResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return assignmentRepository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestTypeProfileAssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
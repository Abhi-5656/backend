package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto.RequestTypeProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.entity.RequestTypeProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.mapper.RequestTypeProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.repository.RequestTypeProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.service.RequestTypeProfileAssignmentService;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeProfileRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public List<RequestTypeProfileAssignmentDTO> assignRequestTypeProfile(RequestTypeProfileAssignmentDTO dto) {
        requestTypeProfileRepository.findById(dto.getRequestTypeProfileId())
                .orElseThrow(() -> new RuntimeException("RequestTypeProfile not found with id: " + dto.getRequestTypeProfileId()));

        List<RequestTypeProfileAssignment> assignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

            RequestTypeProfileAssignment assignment = RequestTypeProfileAssignment.builder()
                    .employeeId(employeeId)
                    .requestTypeProfileId(dto.getRequestTypeProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            assignments.add(assignment);
        }

        List<RequestTypeProfileAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        return savedAssignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestTypeProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
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
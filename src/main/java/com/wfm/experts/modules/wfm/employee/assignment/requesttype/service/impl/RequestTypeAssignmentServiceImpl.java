package com.wfm.experts.modules.wfm.employee.assignment.requesttype.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.requesttype.dto.RequestTypeAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.entity.RequestTypeAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.mapper.RequestTypeAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.repository.RequestTypeAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.service.RequestTypeAssignmentService;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
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
public class RequestTypeAssignmentServiceImpl implements RequestTypeAssignmentService {

    private final RequestTypeAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final RequestTypeAssignmentMapper mapper;

    @Override
    public List<RequestTypeAssignmentDTO> assignRequestType(RequestTypeAssignmentDTO dto) {
        requestTypeRepository.findById(dto.getRequestTypeId())
                .orElseThrow(() -> new RuntimeException("RequestType not found with id: " + dto.getRequestTypeId()));

        List<RequestTypeAssignment> assignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

            RequestTypeAssignment assignment = RequestTypeAssignment.builder()
                    .employeeId(employeeId)
                    .requestTypeId(dto.getRequestTypeId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            assignments.add(assignment);
        }

        List<RequestTypeAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        return savedAssignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestTypeAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestTypeAssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
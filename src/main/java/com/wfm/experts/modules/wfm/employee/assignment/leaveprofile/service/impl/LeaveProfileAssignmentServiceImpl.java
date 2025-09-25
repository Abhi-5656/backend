package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenant.common.employees.entity.Employee;
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
public class LeaveProfileAssignmentServiceImpl implements LeaveProfileAssignmentService {

    private final LeaveProfileAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveProfileAssignmentMapper mapper;
    private final LeaveAccrualService leaveAccrualService;

    @Override
    public List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto) {
        leaveProfileRepository.findById(dto.getLeaveProfileId())
                .orElseThrow(() -> new RuntimeException("LeaveProfile not found with id: " + dto.getLeaveProfileId()));

        List<LeaveProfileAssignment> savedAssignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

            // Prevent re-assignment with the same effective date.
            boolean exists = assignmentRepository.findByEmployeeId(employeeId).stream()
                    .anyMatch(a -> a.isActive() && a.getEffectiveDate().equals(dto.getEffectiveDate()));
            if (exists) {
                throw new IllegalStateException("An active assignment with the same effective date already exists for employee: " + employeeId);
            }

            // Deactivate all existing assignments for this employee.
            List<LeaveProfileAssignment> existingAssignments = assignmentRepository.findByEmployeeId(employeeId);
            for (LeaveProfileAssignment existing : existingAssignments) {
                existing.setActive(false);
            }
            assignmentRepository.saveAll(existingAssignments);

            // Create the new, active assignment.
            LeaveProfileAssignment newAssignment = LeaveProfileAssignment.builder()
                    .employeeId(employeeId)
                    .leaveProfileId(dto.getLeaveProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            savedAssignments.add(assignmentRepository.save(newAssignment));

            // Trigger a full recalculation, which will now find the single active assignment.
            leaveAccrualService.recalculateTotalLeaveBalance(employeeId);
        }

        return savedAssignments.stream()
                .map(mapper::toDto)
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
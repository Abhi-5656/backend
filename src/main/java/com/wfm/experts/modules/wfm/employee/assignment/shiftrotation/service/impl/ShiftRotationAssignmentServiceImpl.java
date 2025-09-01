package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.MultiShiftRotationAssignmentRequestDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.mapper.ShiftRotationAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.repository.ShiftRotationAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.ShiftRotationAssignmentService;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRotationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftRotationAssignmentServiceImpl implements ShiftRotationAssignmentService {

    private final ShiftRotationAssignmentRepository assignmentRepository;
    private final ShiftRotationRepository shiftRotationRepository;
    private final ShiftRotationAssignmentMapper mapper;
    private final EmployeeShiftService employeeShiftService;
    private final EmployeeRepository employeeRepository;


    //    @Override
//    public ShiftRotationAssignment createAssignment(ShiftRotationAssignmentDTO dto) {
//        ShiftRotation rotation = shiftRotationRepository.findById(dto.getShiftRotationId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid ShiftRotation ID"));
//
//        ShiftRotationAssignment entity = mapper.toEntity(dto);
//        entity.setShiftRotation(rotation);
//
//        return assignmentRepository.save(entity);
//    }
//@Override
//public ShiftRotationAssignment createAssignment(ShiftRotationAssignmentDTO dto) {
//
//    // 1. Check employee existence
//    Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(dto.getEmployeeId());
//    if (employeeOpt.isEmpty()) {
//        throw new IllegalArgumentException("Employee does not exist: " + dto.getEmployeeId());
//    }
//
//    ShiftRotation rotation = shiftRotationRepository.findById(dto.getShiftRotationId())
//            .orElseThrow(() -> new IllegalArgumentException("Invalid ShiftRotation ID"));
//
//    ShiftRotationAssignment entity = mapper.toEntity(dto);
//    entity.setShiftRotation(rotation);
//    ShiftRotationAssignment saved = assignmentRepository.save(entity);
//
//    // ✅ Automatically generate roster shifts after assignment
//    LocalDate from = dto.getEffectiveDate();
//    LocalDate to = dto.getExpirationDate() != null ? dto.getExpirationDate() : from.plusWeeks(rotation.getWeeks());
//    employeeShiftService.generateShiftsFromRotation(dto.getEmployeeId(), from, to);
//
//    return saved;
//}


    @Override
    public ShiftRotationAssignment updateAssignment(Long id, ShiftRotationAssignmentDTO dto) {
        ShiftRotationAssignment existing = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        ShiftRotation rotation = shiftRotationRepository.findById(dto.getShiftRotationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ShiftRotation ID"));

        ShiftRotationAssignment updated = mapper.toEntity(dto);
        updated.setId(id);
        updated.setShiftRotation(rotation);

        return assignmentRepository.save(updated);
    }

    @Override
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    public ShiftRotationAssignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    }

    @Override
    public List<ShiftRotationAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    @Transactional
    public List<ShiftRotationAssignment> assignShiftRotationToMultipleEmployees(MultiShiftRotationAssignmentRequestDTO requestDTO) {
        // Fetch ShiftRotation entity once
        ShiftRotation shiftRotation = shiftRotationRepository.findById(requestDTO.getShiftRotationId())
                .orElseThrow(() -> new IllegalArgumentException("ShiftRotation not found: " + requestDTO.getShiftRotationId()));

        // Validate all employees exist (fail-fast if any missing)
        List<String> employeeIds = requestDTO.getEmployees();
        List<String> missingEmployeeIds = employeeIds.stream()
                .filter(eid -> employeeRepository.findByEmployeeId(eid).isEmpty())
                .toList();
        if (!missingEmployeeIds.isEmpty()) {
            throw new IllegalArgumentException("Employee(s) do not exist: " + String.join(", ", missingEmployeeIds));
        }

        // Build all assignment entities
        List<ShiftRotationAssignment> assignments = employeeIds.stream()
                .map(employeeId -> ShiftRotationAssignment.builder()
                        .employeeId(employeeId)
                        .shiftRotation(shiftRotation)
                        .effectiveDate(requestDTO.getEffectiveDate())
                        .expirationDate(requestDTO.getExpirationDate())
                        .build())
                .toList();

        // Bulk insert
        List<ShiftRotationAssignment> savedAssignments = assignmentRepository.saveAll(assignments);

        // Determine roster window
        LocalDate from = requestDTO.getEffectiveDate();
        LocalDate to = requestDTO.getExpirationDate() != null
                ? requestDTO.getExpirationDate()
                : from.plusWeeks(shiftRotation.getWeeks());

        // ✅ Generate shifts for ALL employees in this batch
        employeeShiftService.generateShiftsFromRotation(employeeIds, from, to);

        return savedAssignments;
    }

}

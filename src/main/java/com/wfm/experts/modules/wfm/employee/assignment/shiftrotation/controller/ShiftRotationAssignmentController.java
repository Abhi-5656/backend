package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.controller;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.MultiShiftRotationAssignmentRequestDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.mapper.ShiftRotationAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.ShiftRotationAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/shift-rotation-assignments")
@RequiredArgsConstructor
@Validated // Enable validation for this controller
public class ShiftRotationAssignmentController {

    private final ShiftRotationAssignmentService service;
    private final ShiftRotationAssignmentMapper mapper;

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:assign')")
    public ResponseEntity<List<ShiftRotationAssignmentDTO>> assignShiftRotationToMultipleEmployees(
            @Valid @RequestBody MultiShiftRotationAssignmentRequestDTO requestDTO
    ) {
        List<ShiftRotationAssignment> assignments = service.assignShiftRotationToMultipleEmployees(requestDTO);
        List<ShiftRotationAssignmentDTO> dtos = assignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:update')")
    public ResponseEntity<ShiftRotationAssignmentDTO> update(
            @PathVariable Long id, @Valid @RequestBody ShiftRotationAssignmentDTO dto
    ) {
        ShiftRotationAssignment updated = service.updateAssignment(id, dto);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:read')")
    public ResponseEntity<ShiftRotationAssignmentDTO> get(@PathVariable Long id) {
        ShiftRotationAssignment assignment = service.getAssignment(id);
        return ResponseEntity.ok(mapper.toDto(assignment));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:read')")
    public ResponseEntity<List<ShiftRotationAssignmentDTO>> getAll() {
        List<ShiftRotationAssignment> list = service.getAllAssignments();
        return ResponseEntity.ok(list.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:read') or (hasAuthority('wfm:employee:shift-rotation-assignment:read:own') and #employeeId == authentication.principal.username)")
    public ResponseEntity<List<ShiftRotationAssignmentDTO>> getByEmployee(@PathVariable String employeeId) {
        List<ShiftRotationAssignment> assignments = service.getAllAssignments()
                .stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(assignments.stream().map(mapper::toDto).collect(Collectors.toList()));
    }
}
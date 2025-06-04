package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.controller;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.mapper.ShiftRotationAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.service.ShiftRotationAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/shift-rotation-assignments")
@RequiredArgsConstructor
public class ShiftRotationAssignmentController {

    private final ShiftRotationAssignmentService service;
    private final ShiftRotationAssignmentMapper mapper;

    @PostMapping
    public ResponseEntity<ShiftRotationAssignmentDTO> create(@RequestBody ShiftRotationAssignmentDTO dto) {
        ShiftRotationAssignment saved = service.createAssignment(dto);
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftRotationAssignmentDTO> update(@PathVariable Long id, @RequestBody ShiftRotationAssignmentDTO dto) {
        ShiftRotationAssignment updated = service.updateAssignment(id, dto);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftRotationAssignmentDTO> get(@PathVariable Long id) {
        ShiftRotationAssignment assignment = service.getAssignment(id);
        return ResponseEntity.ok(mapper.toDto(assignment));
    }

    @GetMapping
    public ResponseEntity<List<ShiftRotationAssignmentDTO>> getAll() {
        List<ShiftRotationAssignment> list = service.getAllAssignments();
        return ResponseEntity.ok(list.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}

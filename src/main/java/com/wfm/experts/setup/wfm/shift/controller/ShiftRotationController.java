package com.wfm.experts.setup.wfm.shift.controller;

import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/shift-rotations")
@RequiredArgsConstructor
public class ShiftRotationController {

    private final ShiftRotationService shiftRotationService;

    @PostMapping
    public ResponseEntity<ShiftRotationDTO> create(@RequestBody ShiftRotationDTO dto) {
        return ResponseEntity.ok(shiftRotationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftRotationDTO> update(@PathVariable Long id, @RequestBody ShiftRotationDTO dto) {
        return ResponseEntity.ok(shiftRotationService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftRotationDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(shiftRotationService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ShiftRotationDTO>> getAll() {
        return ResponseEntity.ok(shiftRotationService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftRotationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

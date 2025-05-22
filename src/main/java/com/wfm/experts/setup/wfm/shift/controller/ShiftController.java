package com.wfm.experts.setup.wfm.shift.controller;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import com.wfm.experts.setup.wfm.shift.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<ShiftDTO> createShift(@RequestBody ShiftDTO dto) {
        return ResponseEntity.ok(shiftService.createShift(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftDTO> updateShift(@PathVariable Long id, @RequestBody ShiftDTO dto) {
        return ResponseEntity.ok(shiftService.updateShift(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftDTO> getShift(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShift(id));
    }

    @GetMapping
    public ResponseEntity<List<ShiftDTO>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}

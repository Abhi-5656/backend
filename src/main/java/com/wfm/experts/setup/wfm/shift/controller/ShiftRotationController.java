//package com.wfm.experts.setup.wfm.shift.controller;
//
//import com.wfm.experts.setup.wfm.controller.WfmSetupController;
//import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
//import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/setup/wfm/shift-rotations")
//@RequiredArgsConstructor
//public class ShiftRotationController extends WfmSetupController {
//
//    private final ShiftRotationService shiftRotationService;
//
//    @PostMapping
//    public ResponseEntity<ShiftRotationDTO> create(@RequestBody ShiftRotationDTO dto) {
//        return ResponseEntity.ok(shiftRotationService.create(dto));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ShiftRotationDTO> update(@PathVariable Long id, @RequestBody ShiftRotationDTO dto) {
//        return ResponseEntity.ok(shiftRotationService.update(id, dto));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ShiftRotationDTO> get(@PathVariable Long id) {
//        return ResponseEntity.ok(shiftRotationService.get(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ShiftRotationDTO>> getAll() {
//        return ResponseEntity.ok(shiftRotationService.getAll());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        shiftRotationService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
package com.wfm.experts.setup.wfm.shift.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/shift-rotations")
@RequiredArgsConstructor
public class ShiftRotationController extends WfmSetupController {

    private final ShiftRotationService shiftRotationService;

    @PostMapping
    @PreAuthorize("hasAuthority('wfm:setup:shift-rotation:create')")
    public ResponseEntity<ShiftRotationDTO> create(@RequestBody ShiftRotationDTO dto) {
        return ResponseEntity.ok(shiftRotationService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:shift-rotation:update')")
    public ResponseEntity<ShiftRotationDTO> update(@PathVariable Long id, @RequestBody ShiftRotationDTO dto) {
        return ResponseEntity.ok(shiftRotationService.update(id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:shift-rotation:read')")
    public ResponseEntity<ShiftRotationDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(shiftRotationService.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:setup:shift-rotation:read')")
    public ResponseEntity<List<ShiftRotationDTO>> getAll() {
        return ResponseEntity.ok(shiftRotationService.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:shift-rotation:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftRotationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
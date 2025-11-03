package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/holiday-profile-assignments")
@RequiredArgsConstructor
@Validated // Enable validation for this controller
public class HolidayProfileAssignmentController {

    private final HolidayProfileAssignmentService service;

    /**
     * Assign a HolidayProfile to multiple employees.
     * Returns one DTO per created assignment.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:assign')")
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> assignHolidayProfiles(
            @Valid @RequestBody HolidayProfileAssignmentDTO dto) { // Added @Valid
        List<HolidayProfileAssignmentDTO> created = service.assignHolidayProfiles(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Get all HolidayProfileAssignments.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:read')")
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> getAllAssignments() {
        List<HolidayProfileAssignmentDTO> all = service.getAllAssignments();
        return ResponseEntity.ok(all);
    }

    /**
     * Get a single HolidayProfileAssignment by its ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:read')")
    public ResponseEntity<HolidayProfileAssignmentDTO> getAssignmentById(
            @PathVariable Long id) {
        HolidayProfileAssignmentDTO dto = service.getAssignmentById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get all HolidayProfileAssignments for a given employee ID.
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:read')")
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> getByEmployee(
            @PathVariable String employeeId) {
        List<HolidayProfileAssignmentDTO> list = service.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    /**
     * Deactivate a HolidayProfileAssignment by its ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:delete')")
    public ResponseEntity<Void> deactivateAssignment(@PathVariable Long id) { // Renamed method
        service.deactivateAssignment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * NEW: Explicitly set an expiration date for an assignment.
     */
    @PutMapping("/{id}/expire")
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:update')") // Assuming update permission
    public ResponseEntity<HolidayProfileAssignmentDTO> expireAssignment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {

        HolidayProfileAssignmentDTO updatedDto = service.expireAssignment(id, expirationDate);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * Get all assigned holidays for a given employee ID.
     */
    @GetMapping("/employee/{employeeId}/holidays")
    @PreAuthorize("hasAuthority('wfm:employee:holiday-profile-assignment:read')")
    public ResponseEntity<Map<String, Object>> getAssignedHolidays(@PathVariable String employeeId) {
        List<HolidayDTO> holidays = service.getAssignedHolidaysByEmployeeId(employeeId);
        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        response.put("holidays", holidays);
        return ResponseEntity.ok(response);
    }
}
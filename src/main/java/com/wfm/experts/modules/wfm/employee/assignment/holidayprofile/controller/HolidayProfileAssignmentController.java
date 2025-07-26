package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.controller;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/holiday-profile-assignments")
@RequiredArgsConstructor
public class HolidayProfileAssignmentController {

    private final HolidayProfileAssignmentService service;

    /**
     * Assign a HolidayProfile to multiple employees.
     * Returns one DTO per created assignment.
     */
    @PostMapping
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> assignHolidayProfiles(
            @RequestBody HolidayProfileAssignmentDTO dto) {
        List<HolidayProfileAssignmentDTO> created = service.assignHolidayProfiles(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Get all HolidayProfileAssignments.
     */
    @GetMapping
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> getAllAssignments() {
        List<HolidayProfileAssignmentDTO> all = service.getAllAssignments();
        return ResponseEntity.ok(all);
    }

    /**
     * Get a single HolidayProfileAssignment by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HolidayProfileAssignmentDTO> getAssignmentById(
            @PathVariable Long id) {
        HolidayProfileAssignmentDTO dto = service.getAssignmentById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get all HolidayProfileAssignments for a given employee ID.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<HolidayProfileAssignmentDTO>> getByEmployee(
            @PathVariable String employeeId) {
        List<HolidayProfileAssignmentDTO> list = service.getAssignmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    /**
     * Deactivate (soft-delete) a HolidayProfileAssignment by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        service.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all assigned holidays for a given employee ID.
     */
    @GetMapping("/employee/{employeeId}/holidays")
    public ResponseEntity<Map<String, Object>> getAssignedHolidays(@PathVariable String employeeId) {
        List<HolidayDTO> holidays = service.getAssignedHolidaysByEmployeeId(employeeId);
        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        response.put("holidays", holidays);
        return ResponseEntity.ok(response);
    }
}

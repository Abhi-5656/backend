package com.wfm.experts.modules.wfm.features.timesheet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wfm/timesheets")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;

    @PostMapping
    public ResponseEntity<TimesheetDTO> createTimesheet(@RequestBody TimesheetDTO timesheetDTO) throws JsonProcessingException {
        TimesheetDTO created = timesheetService.createTimesheet(timesheetDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimesheetDTO> updateTimesheet(@PathVariable Long id,
                                                        @RequestBody TimesheetDTO timesheetDTO) {
        TimesheetDTO updated = timesheetService.updateTimesheet(id, timesheetDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TimesheetDTO>> createTimesheets(@RequestBody List<TimesheetDTO> timesheetDTOs) {
        List<TimesheetDTO> created = timesheetService.createTimesheets(timesheetDTOs);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimesheetDTO> getById(@PathVariable Long id) {
        Optional<TimesheetDTO> timesheet = timesheetService.getTimesheetById(id);
        return timesheet.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}/date/{workDate}")
    public ResponseEntity<TimesheetDTO> getByEmployeeAndDate(@PathVariable String employeeId,
                                                             @PathVariable String workDate) {
        Optional<TimesheetDTO> timesheet = timesheetService.getTimesheetByEmployeeAndDate(
                employeeId, LocalDate.parse(workDate));
        return timesheet.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<List<TimesheetDTO>> getByEmployeeAndDateRange(
            @PathVariable String employeeId,
            @RequestParam String start,
            @RequestParam String end) {
        List<TimesheetDTO> timesheets = timesheetService.getTimesheetsByEmployeeAndDateRange(
                employeeId, LocalDate.parse(start), LocalDate.parse(end));
        return ResponseEntity.ok(timesheets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimesheet(@PathVariable Long id) {
        timesheetService.deleteTimesheet(id);
        return ResponseEntity.noContent().build();
    }
}

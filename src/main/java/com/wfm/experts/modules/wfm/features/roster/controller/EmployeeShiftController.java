package com.wfm.experts.modules.wfm.features.roster.controller;

import com.wfm.experts.modules.wfm.features.roster.dto.BulkEmployeeShiftUpdateRequestDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterDTO;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/shifts")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

//    @GetMapping
//    public List<EmployeeShiftDTO> getShifts(
//            @RequestParam("employeeId") String employeeId,
//            @RequestParam("start") String start,
//            @RequestParam("end") String end
//    ) {
//        return employeeShiftService.getShiftsForEmployeeInRange(
//                employeeId,
//                LocalDate.parse(start),
//                LocalDate.parse(end)
//        );
//    }



    @GetMapping("/employee-shift-roster")
    public List<EmployeeShiftRosterDTO> getEmployeeRosterForDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return employeeShiftService.getEmployeeRosterForDateRange(startDate, endDate);
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssignOrUpdateShifts(
            @RequestBody BulkEmployeeShiftUpdateRequestDTO request
    ) {
        employeeShiftService.bulkAssignOrUpdateShifts(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get roster for a specific employee in the given date range.
     */
    @GetMapping("/employee-shift-roster/{employeeId}")
    public List<EmployeeShiftRosterDTO> getRosterForEmployee(
            @PathVariable String employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return employeeShiftService.getRosterForEmployeeByDateRange(employeeId, startDate, endDate);
    }

}

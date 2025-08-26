//package com.wfm.experts.modules.wfm.features.roster.controller;
//
//import com.wfm.experts.modules.wfm.features.roster.dto.BulkEmployeeShiftUpdateRequestDTO;
//import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
//import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterDTO;
//import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/employee/shifts")
//@RequiredArgsConstructor
//public class EmployeeShiftController {
//
//    private final EmployeeShiftService employeeShiftService;
//
////    @GetMapping
////    public List<EmployeeShiftDTO> getShifts(
////            @RequestParam("employeeId") String employeeId,
////            @RequestParam("start") String start,
////            @RequestParam("end") String end
////    ) {
////        return employeeShiftService.getShiftsForEmployeeInRange(
////                employeeId,
////                LocalDate.parse(start),
////                LocalDate.parse(end)
////        );
////    }
//
//
//
//    @GetMapping("/employee-shift-roster")
//    public List<EmployeeShiftRosterDTO> getEmployeeRosterForDateRange(
//            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        return employeeShiftService.getEmployeeRosterForDateRange(startDate, endDate);
//    }
//
//    @PostMapping("/bulk-assign")
//    public ResponseEntity<Void> bulkAssignOrUpdateShifts(
//            @RequestBody BulkEmployeeShiftUpdateRequestDTO request
//    ) {
//        employeeShiftService.bulkAssignOrUpdateShifts(request);
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * Get roster for a specific employee in the given date range.
//     */
//    @GetMapping("/employee-shift-roster/{employeeId}")
//    public List<EmployeeShiftRosterDTO> getRosterForEmployee(
//            @PathVariable String employeeId,
//            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        return employeeShiftService.getRosterForEmployeeByDateRange(employeeId, startDate, endDate);
//    }
//
//}
package com.wfm.experts.modules.wfm.features.roster.controller;

import com.wfm.experts.modules.wfm.features.roster.dto.BulkEmployeeShiftUpdateRequestDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterDTO;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Import this
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/shifts")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    /**
     * Get the roster for all employees. Requires permission to see all roster data.
     */
    @GetMapping("/employee-shift-roster")
    @PreAuthorize("hasAuthority('wfm:roster:readAll')")
    public List<EmployeeShiftRosterDTO> getEmployeeRosterForDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return employeeShiftService.getEmployeeRosterForDateRange(startDate, endDate);
    }

    /**
     * Bulk assign or update shifts. Requires either assign or update permission.
     */
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:assign') or hasAuthority('wfm:employee:shift-rotation-assignment:update')")
    public ResponseEntity<Void> bulkAssignOrUpdateShifts(
            @RequestBody BulkEmployeeShiftUpdateRequestDTO request
    ) {
        employeeShiftService.bulkAssignOrUpdateShifts(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get roster for a specific employee. Requires permission to read individual assignments.
     */
    @GetMapping("/employee-shift-roster/{employeeId}")
    @PreAuthorize("hasAuthority('wfm:employee:shift-rotation-assignment:read')")
    public List<EmployeeShiftRosterDTO> getRosterForEmployee(
            @PathVariable String employeeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return employeeShiftService.getRosterForEmployeeByDateRange(employeeId, startDate, endDate);
    }

}
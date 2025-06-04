package com.wfm.experts.modules.wfm.features.roster.controller;

import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/shifts")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    @GetMapping
    public List<EmployeeShiftDTO> getShifts(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        return employeeShiftService.getShiftsForEmployeeInRange(
                employeeId,
                LocalDate.parse(start),
                LocalDate.parse(end)
        );
    }
}

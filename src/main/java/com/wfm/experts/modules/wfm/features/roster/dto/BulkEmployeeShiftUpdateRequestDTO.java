package com.wfm.experts.modules.wfm.features.roster.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkEmployeeShiftUpdateRequestDTO {

    // List of employee IDs to update
    private List<String> employeeIds;

    // List of calendar dates for which to assign the shift
    private List<LocalDate> calendarDates;

    // The shift ID to assign (can be null to unassign/mark as off/holiday)
    private Long shiftId;

    // Who performed the assignment
    private String assignedBy;
}

package com.wfm.experts.modules.wfm.features.roster.dto;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShiftRosterDTO {
    private String employeeId;
    private String fullName;
    private LocalDate calendarDate;
    private ShiftDTO shift; // Full shift info, or null if not assigned

    private Boolean isWeekOff = false;  // True if employee is off that day
    private Boolean isHoliday = false;  // True if the day is a holiday
    private String weekday;
    private Boolean deleted;
    private String assignedBy;
}

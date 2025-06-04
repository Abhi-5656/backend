package com.wfm.experts.modules.wfm.features.roster.dto;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShiftDTO {

    private Long id;
    private String employeeId;
    private ShiftDTO shift;
    private String shiftName;      // Useful for UI display, can be derived from shift
    private LocalDate calendarDate;
    private Boolean isWeekOff;
    private Boolean isHoliday;
    private String weekday;        // "MONDAY", "TUESDAY", etc.
    private Boolean deleted;
    private String assignedBy;     // User/system assigning the shift
}

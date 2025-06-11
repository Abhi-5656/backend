package com.wfm.experts.modules.wfm.features.roster.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EmployeeShiftRosterProjection {
    String getEmployeeId();
    String getFullName();
    LocalDate getCalendarDate();
    Long getShiftId();
    String getShiftName();
    LocalTime getShiftStartTime();
    LocalTime getShiftEndTime();
    String getShiftColor();
    Boolean getIsWeekOff();     // Added
    Boolean getIsHoliday();     // Added
    String getWeekday();
    Boolean getDeleted();      // <--- Added
    String getAssignedBy();
}

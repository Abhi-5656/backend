package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayProfileAssignmentDTO {
    private Long id;
    private Long holidayProfileId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private Boolean isActive;
    private List<String> employeeIds;
}

package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Holiday Profile ID cannot be null.")
    private Long holidayProfileId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;
    private Boolean isActive;

    @NotEmpty(message = "Employee IDs list cannot be null or empty.")
    private List<String> employeeIds;
}
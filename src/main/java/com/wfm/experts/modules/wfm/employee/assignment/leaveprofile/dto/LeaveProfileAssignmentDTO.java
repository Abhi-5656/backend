package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveProfileAssignmentDTO {

    private Long id;

    @NotEmpty(message = "Employee IDs list cannot be null or empty.")
    private List<String> employeeIds;

    @NotNull(message = "Leave Profile ID cannot be null.")
    private Long leaveProfileId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    private boolean active;
}
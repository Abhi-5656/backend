package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto;

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
public class RequestTypeProfileAssignmentDTO {

    private Long id;

    @NotEmpty(message = "Employee IDs list cannot be null or empty.")
    private List<String> employeeIds;

    @NotNull(message = "Request Type Profile ID cannot be null.")
    private Long requestTypeProfileId;

    @NotNull(message = "Effective date cannot be null.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;
    private boolean active;
}
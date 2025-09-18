package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto;

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
    private List<String> employeeIds;
    private Long requestTypeProfileId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private boolean active;
}
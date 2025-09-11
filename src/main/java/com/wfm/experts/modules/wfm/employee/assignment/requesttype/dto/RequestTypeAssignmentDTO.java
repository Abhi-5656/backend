package com.wfm.experts.modules.wfm.employee.assignment.requesttype.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTypeAssignmentDTO {

    private Long id;
    private List<String> employeeIds;
    private Long requestTypeId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private boolean active;
}
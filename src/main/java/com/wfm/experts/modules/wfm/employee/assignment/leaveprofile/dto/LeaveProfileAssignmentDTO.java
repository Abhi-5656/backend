package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveProfileAssignmentDTO {

    private Long id;
    private List<String> employeeIds;
    private Long leaveProfileId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private boolean active;
}
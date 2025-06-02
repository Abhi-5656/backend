package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRuleDTO {

    private Long id;
    private Boolean enabled;
    private Integer fullDayHours;
    private Integer fullDayMinutes;
    private Integer halfDayHours;
    private Integer halfDayMinutes;
}

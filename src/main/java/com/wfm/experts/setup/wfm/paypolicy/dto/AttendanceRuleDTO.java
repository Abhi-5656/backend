//package com.wfm.experts.setup.wfm.paypolicy.dto;
//
//import lombok.*;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class AttendanceRuleDTO {
//
//    private Long id;
//    private Boolean enabled;
//    private Integer fullDayHours;
//    private Integer fullDayMinutes;
//    private Integer halfDayHours;
//    private Integer halfDayMinutes;
//}
package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceRuleMode;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRuleDTO {

    private Long id;

    // --- A Set to hold the enabled modes (UNSCHEDULED, SCHEDULED, or both) ---
    private Set<AttendanceRuleMode> enabledModes;

    // -- Unscheduled Settings --
    private Integer unscheduledFullDayHours;
    private Integer unscheduledFullDayMinutes;
    private Integer unscheduledHalfDayHours;
    private Integer unscheduledHalfDayMinutes;

    // -- Scheduled Settings --
    private Integer scheduledFullDayPercentage;
    private Integer scheduledHalfDayPercentage;
}

package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRulesDTO {
    private Long id;
    private boolean enabled;
    private Integer thresholdHours;
    private Integer thresholdMinutes;
    private Double maxOtPerDay;
    private Double maxOtPerWeek;
    private OvertimeConflictResolution conflictResolution;
    private boolean resetOtBucketDaily;
    private boolean resetOtBucketWeekly;
    private boolean resetOtBucketOnPayPeriod;
    private CompensationMethod compensationMethod;
    private Double paidOtMultiplier;
    private Integer compOffDaysPerOt;
    private Integer compOffHoursPerOt;
    private Integer maxCompOffBalance;
    private CompOffBalanceBasis maxCompOffBalanceBasis;
    private Integer compOffExpiryValue;
    private ExpiryUnit compOffExpiryUnit;
    private boolean encashOnExpiry;
    private PreShiftInclusionDTO preShiftInclusion;
    private List<Shift> shifts;
    private DailyOtTrigger dailyOtTrigger;
    private Integer gracePeriodAfterShiftEnd;
    private boolean enableWeeklyOt;
    private Integer weeklyThresholdHours;
    private WeeklyOtBasis weeklyOtBasis;
    private DailyWeeklyOtConflict dailyWeeklyOtConflict;
    private WeekDay weeklyResetDay;
}
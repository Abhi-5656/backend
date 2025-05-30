package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import lombok.*;

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
}

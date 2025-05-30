package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayPayRulesDTO {
    private Long id;
    private boolean enabled;
    private HolidayPayType holidayPayType;
    private Double payMultiplier;
    private Integer minHoursForCompOff;
    private CompOffBalanceBasis maxCompOffBalanceBasis;
    private Integer maxCompOffBalance;
    private Integer compOffExpiryValue;
    private ExpiryUnit compOffExpiryUnit;
    private boolean encashOnExpiry;
}

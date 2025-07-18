package com.wfm.experts.setup.wfm.paypolicy.dto;


import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
import com.wfm.experts.setup.wfm.paypolicy.enums.WeekDay;
import com.wfm.experts.setup.wfm.paypolicy.enums.WeekendPayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekendPayRulesDTO {
    private Long id;
    private boolean enabled;
    private Set<WeekDay> weekendDays;
    private WeekendPayType weekendPayType;
    private Double payMultiplier;
    private Integer minHoursForCompOff;
    private CompOffBalanceBasis maxCompOffBalanceBasis;
    private Integer maxCompOffBalance;
    private Integer compOffExpiryValue;
    private ExpiryUnit compOffExpiryUnit;
    private boolean encashOnExpiry;
}
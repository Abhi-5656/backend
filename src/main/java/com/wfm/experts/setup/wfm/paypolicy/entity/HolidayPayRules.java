package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "holiday_pay_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayPayRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HolidayPayType holidayPayType;

    private Double payMultiplier;
    private Integer minHoursForCompOff;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompOffBalanceBasis maxCompOffBalanceBasis;

    private Integer maxCompOffBalance;
    private Integer compOffExpiryValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ExpiryUnit compOffExpiryUnit;

    private boolean encashOnExpiry;
}

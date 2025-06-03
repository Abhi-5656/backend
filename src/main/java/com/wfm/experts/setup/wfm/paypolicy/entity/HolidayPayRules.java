package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "holiday_pay_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayPayRules implements PayPolicyRule {
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

    // ----------- PayPolicyRule implementation ---------------

    @Override
    public String getName() {
        return "HolidayPayRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Only run if enabled (customize as needed)
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // TODO: implement holiday pay logic
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("NOT_IMPLEMENTED")
                .success(true)
                .message("HolidayPayRules executed - implement your logic here.")
                .build();
    }
}

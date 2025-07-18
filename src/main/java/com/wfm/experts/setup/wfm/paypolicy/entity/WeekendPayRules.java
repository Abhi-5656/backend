package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
import com.wfm.experts.setup.wfm.paypolicy.enums.WeekDay;
import com.wfm.experts.setup.wfm.paypolicy.enums.WeekendPayType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "weekend_pay_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekendPayRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * Stores the days of the week considered as weekends for this rule.
     * This is mapped to a separate table 'weekend_pay_rule_days'.
     */
    @ElementCollection(targetClass = WeekDay.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "weekend_pay_rule_days", joinColumns = @JoinColumn(name = "rule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "weekend_day", nullable = false)
    private Set<WeekDay> weekendDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "weekend_pay_type")
    private WeekendPayType weekendPayType;

    @Column(name = "pay_multiplier")
    private Double payMultiplier;

    @Column(name = "min_hours_for_comp_off")
    private Integer minHoursForCompOff;

    @Enumerated(EnumType.STRING)
    @Column(name = "max_comp_off_balance_basis")
    private CompOffBalanceBasis maxCompOffBalanceBasis;

    @Column(name = "max_comp_off_balance")
    private Integer maxCompOffBalance;

    @Column(name = "comp_off_expiry_value")
    private Integer compOffExpiryValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "comp_off_expiry_unit")
    private ExpiryUnit compOffExpiryUnit;

    @Column(name = "encash_on_expiry", nullable = false)
    private boolean encashOnExpiry;



    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return false;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        return null;
    }
}
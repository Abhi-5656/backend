package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.PayCalculationType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "pay_period_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPeriodRules implements PayPolicyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PayCalculationType periodType;

    @Column(length = 10)
    private String referenceDate; // yyyy-MM-dd

    @Column(length = 10)
    private String weekStart;     // "SUNDAY" or "MONDAY"

    @ElementCollection
    @CollectionTable(name = "pay_period_semi_monthly_days", joinColumns = @JoinColumn(name = "pay_period_rules_id"))
    @Column(name = "day")
    private List<Integer> semiMonthlyDays;

    // --- Implement PayPolicyRule ---
    @Override
    public String getName() {
        return "PayPeriodRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // TODO: implement logic for pay period validation/calculation
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("NOT_IMPLEMENTED")
                .success(true)
                .message("PayPeriodRules executed - implement your logic here.")
                .build();
    }
}

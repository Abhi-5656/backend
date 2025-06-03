package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rounding_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundingRules implements PayPolicyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoundingRuleScope scope;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "clock_in_rule_id")
    private RoundingRule clockIn;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "clock_out_rule_id")
    private RoundingRule clockOut;

    // --- PayPolicyRule interface implementation ---

    @Override
    public String getName() {
        return "RoundingRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Example: Only run if enabled
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // Put your rounding logic here (call clockIn/clockOut rule objects, etc)
        // This is a placeholder—actual logic will depend on your design

        boolean roundingApplied = enabled; // Replace with real evaluation
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(roundingApplied ? "ROUNDED" : "NOT_APPLIED")
                .success(roundingApplied)
                .message("Rounding rules evaluated")
                .build();
    }
}

package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "break_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private boolean allowMultiple;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "break_rules_id")
    private List<Break> breaks;

    // --- Implement PayPolicyRule interface ---

    @Override
    public String getName() {
        return "BreakRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Only run if enabled (stub for now)
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // Replace this stub with your real break rules logic
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("NOT_IMPLEMENTED")
                .success(true)
                .message("BreakRules executed - implement real logic here.")
                .build();
    }
}

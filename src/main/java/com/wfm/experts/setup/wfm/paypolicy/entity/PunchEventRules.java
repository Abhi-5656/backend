package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "punch_event_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEventRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private Integer earlyIn;
    private Integer lateIn;
    private Integer earlyOut;
    private Integer lateOut;
    private boolean notifyOnPunchEvents;

    // --- PayPolicyRule interface implementation ---

    @Override
    public String getName() {
        return "PunchEventRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Implement your evaluation logic (e.g., is this rule enabled and context has punches)
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // Implement your rule logic (example skeleton, customize as needed)
        // Evaluate punch events for early/late IN/OUT and return a result

        boolean passed = true; // Your punch evaluation logic here

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(passed ? "PASS" : "FAIL")
                .success(passed)
                .message("Punch event rules evaluated.")
                .build();
    }
}

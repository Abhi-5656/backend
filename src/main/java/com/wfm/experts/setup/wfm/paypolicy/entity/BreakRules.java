package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.BreakType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
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
        // Rule should only run if it's enabled and there are breaks defined.
        return enabled && breaks != null && !breaks.isEmpty();
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // Calculate the total duration of all defined unpaid breaks.
        int totalUnpaidBreakMinutes = breaks.stream()
                .filter(b -> b.getType() == BreakType.UNPAID && b.getDuration() != null && b.getDuration() > 0)
                .mapToInt(Break::getDuration)
                .sum();

        if (totalUnpaidBreakMinutes > 0) {
            context.getFacts().put("unpaidBreakMinutes", totalUnpaidBreakMinutes);
            String message = String.format("Calculated %d minutes of unpaid breaks.", totalUnpaidBreakMinutes);
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("UNPAID_BREAKS_CALCULATED")
                    .success(true)
                    .message(message)
                    .build();
        }

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("NO_UNPAID_BREAKS")
                .success(true)
                .message("No unpaid breaks to deduct.")
                .build();
    }
}
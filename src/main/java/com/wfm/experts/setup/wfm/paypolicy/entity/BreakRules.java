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
        // Rule should only run if it's enabled and there are breaks defined.
        return enabled && breaks != null && !breaks.isEmpty();
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");

        // If there's no work time calculated yet, there's nothing to deduct from.
        if (workedMinutes == null || workedMinutes <= 0) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("NOT_APPLICABLE")
                    .success(true)
                    .message("No worked minutes to apply break deduction to.")
                    .build();
        }

        // Calculate the total duration of all defined unpaid breaks.
        int totalUnpaidBreakMinutes = breaks.stream()
                .filter(b -> b.getDuration() != null && b.getDuration() > 0)
                .mapToInt(Break::getDuration)
                .sum();

        if (totalUnpaidBreakMinutes <= 0) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("NO_BREAKS_DEFINED")
                    .success(true)
                    .message("No break durations are defined in the policy.")
                    .build();
        }

        // Deduct the total break time from the worked minutes.
        int netWorkMinutes = Math.max(0, workedMinutes - totalUnpaidBreakMinutes);

        // Update the 'workedMinutes' fact in the context so subsequent rules (like overtime)
        // use the adjusted value.
        context.getFacts().put("workedMinutes", netWorkMinutes);

        String message = String.format("Deducted %d minutes for unpaid breaks. Original: %d mins, Net: %d mins.",
                totalUnpaidBreakMinutes, workedMinutes, netWorkMinutes);

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("BREAKS_DEDUCTED")
                .success(true)
                .message(message)
                .build();
    }
}
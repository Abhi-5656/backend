package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private LocalDateTime applyRounding(LocalDateTime original, RoundingRule rule) {
        if (rule == null || rule.getInterval() == null || rule.getInterval() <= 0) return original;
        int interval = rule.getInterval();
        int minute = original.getMinute();
        int roundedMinute;
        switch (rule.getType()) {
            case UP:
                roundedMinute = ((minute + interval - 1) / interval) * interval;
                break;
            case DOWN:
                roundedMinute = (minute / interval) * interval;
                break;
            case NEAREST:
            default:
                roundedMinute = ((minute + interval / 2) / interval) * interval;
                break;
        }
        // Apply grace period logic: If within grace, do not round
        Integer grace = rule.getGracePeriod();
        if (grace != null && Math.abs(roundedMinute - minute) <= grace) {
            return original;
        }
        return original.withMinute(roundedMinute).withSecond(0).withNano(0);
    }


    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        if (!enabled) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("NOT_APPLIED")
                    .success(false)
                    .message("Rounding not enabled.")
                    .build();
        }

        // Track what was rounded
        StringBuilder msg = new StringBuilder("Rounding results: ");
        boolean anyRounded = false;

        for (var punch : context.getPunchEvents()) {
            boolean shouldRound = false;
            LocalDateTime original = punch.getEventTime();
            LocalDateTime rounded = original;
            String punchType = punch.getPunchType().name();

            if (scope == RoundingRuleScope.BOTH || (scope == RoundingRuleScope.CLOCK_IN && "IN".equals(punchType))) {
                if ("IN".equals(punchType) && clockIn != null) {
                    rounded = applyRounding(original, clockIn);
                    shouldRound = true;
                }
            }
            if (scope == RoundingRuleScope.BOTH || (scope == RoundingRuleScope.CLOCK_OUT && "OUT".equals(punchType))) {
                if ("OUT".equals(punchType) && clockOut != null) {
                    rounded = applyRounding(original, clockOut);
                    shouldRound = true;
                }
            }
            if (shouldRound && !rounded.equals(original)) {
                msg.append("[").append(punchType).append(" punch: ")
                        .append(original.toLocalTime()).append(" → ")
                        .append(rounded.toLocalTime()).append("] ");
                punch.setEventTime(rounded); // Update for further rule calculations if needed
                anyRounded = true;
            }
        }

        if (!anyRounded) {
            msg.append("No punches required rounding.");
        }

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(anyRounded ? "ROUNDED" : "NOT_APPLIED")
                .success(true)
                .message(msg.toString().trim())
                .build();
    }

}

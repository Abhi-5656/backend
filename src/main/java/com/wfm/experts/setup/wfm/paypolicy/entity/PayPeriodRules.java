package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.PayCalculationType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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
    private String referenceDate; // "YYYY-MM-DD"

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
        // Rule is applicable only if it's enabled and has a type configured.
        return enabled && periodType != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        LocalDate currentDate = context.getDate();
        boolean isPayPeriodEnd = false;
        String message = "Pay period end check. ";

        try {
            switch (periodType) {
                case WEEKLY:
                    if (this.weekStart == null) {
                        message += "Weekly check failed: Week Start day not set.";
                        break;
                    }
                    DayOfWeek weekStartDay = DayOfWeek.valueOf(this.weekStart.toUpperCase());
                    // The pay period ends on the day before the week starts.
                    if (currentDate.getDayOfWeek() == weekStartDay.minus(1)) {
                        isPayPeriodEnd = true;
                    }
                    message += "Weekly period ends on " + weekStartDay.minus(1) + ".";
                    break;

                case BIWEEKLY:
                    if (this.referenceDate == null) {
                        message += "Bi-Weekly check failed: Reference date not set.";
                        break;
                    }
                    LocalDate refDateBiWeekly = LocalDate.parse(this.referenceDate);
                    // A bi-weekly period is 14 days long. The end is on the 13th day of a 0-13 day cycle.
                    long daysBetween = ChronoUnit.DAYS.between(refDateBiWeekly, currentDate);
                    if (daysBetween >= 0 && (daysBetween + 1) % 14 == 0) {
                        isPayPeriodEnd = true;
                    }
                    message += "Bi-Weekly check against reference " + this.referenceDate + ".";
                    break;

                case MONTHLY:
                    if (currentDate.getDayOfMonth() == currentDate.lengthOfMonth()) {
                        isPayPeriodEnd = true;
                    }
                    message += "Monthly period ends on the last day of the month.";
                    break;

                case SEMIMONTHLY:
                    if (this.semiMonthlyDays == null || this.semiMonthlyDays.size() != 2) {
                        message += "Semi-Monthly check failed: Two split days are not configured.";
                        break;
                    }
                    int dayOfMonth = currentDate.getDayOfMonth();
                    int firstSplit = this.semiMonthlyDays.get(0);
                    int secondSplit = this.semiMonthlyDays.get(1);
                    // The period ends on the first split day, or on the last day of the month if it's past the second split day.
                    if (dayOfMonth == firstSplit || (dayOfMonth == currentDate.lengthOfMonth() && dayOfMonth >= secondSplit) ) {
                        isPayPeriodEnd = true;
                    }
                    message += "Semi-Monthly check for days " + firstSplit + " and end of month (from day " + secondSplit + " onwards).";
                    break;
            }
        } catch (DateTimeParseException e) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("CONFIG_ERROR")
                    .success(false)
                    .message("Error parsing reference date: " + this.referenceDate)
                    .build();
        } catch (IllegalArgumentException e) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("CONFIG_ERROR")
                    .success(false)
                    .message("Error in configuration: " + e.getMessage())
                    .build();
        }

        // Update the execution context with the result.
        context.getFacts().put("isPayPeriodEnd", isPayPeriodEnd);

        if (isPayPeriodEnd) {
            message += " Today is the end of a pay period.";
        } else {
            message += " Today is not the end of a pay period.";
        }

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(isPayPeriodEnd ? "PAY_PERIOD_END" : "PAY_PERIOD_MID")
                .success(true)
                .message(message)
                .build();
    }
}
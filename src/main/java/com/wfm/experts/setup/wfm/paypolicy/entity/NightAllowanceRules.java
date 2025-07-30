package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "night_allowance_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NightAllowanceRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Column(name = "start_time", length = 10)
    private String startTime; // e.g., "22:00"

    @Column(name = "end_time", length = 10)
    private String endTime;   // e.g., "06:00"

    @Column(name = "pay_multiplier")
    private Double payMultiplier;

    @Override
    public String getName() {
        return "NightAllowanceRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Rule is applicable only if it's enabled and there's worked time.
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        if (workedMinutes == null || workedMinutes <= 0) {
            return buildResult("NO_WORK_TIME", true, "No work time for night allowance calculation.");
        }

        List<PunchEvent> punches = context.getPunchEvents();
        if (punches == null || punches.size() < 2) {
            return buildResult("INSUFFICIENT_PUNCHES", true, "Not enough punch events to determine work duration.");
        }

        // Sort punches to reliably find the start and end of the work period
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
        LocalDateTime workStart = punches.get(0).getEventTime();
        LocalDateTime workEnd = punches.get(punches.size() - 1).getEventTime();

        LocalTime nightStart = LocalTime.parse(startTime);
        LocalTime nightEnd = LocalTime.parse(endTime);

        long nightMinutes = calculateNightMinutes(workStart, workEnd, nightStart, nightEnd);

        if (nightMinutes <= 0) {
            return buildResult("NO_NIGHT_WORK", true, "Work did not occur during the night allowance period.");
        }

        // Example calculation: fixed amount per hour of night work multiplied by the multiplier
        double allowanceRatePerHour = 100.0; // This could be a configurable value
        double allowance = (nightMinutes / 60.0) * allowanceRatePerHour * (payMultiplier != null ? payMultiplier : 1.0);

        context.getFacts().put("nightAllowanceAmount", allowance);
        context.getFacts().put("nightWorkedMinutes", (int) nightMinutes);

        String message = String.format(
                "Applied night allowance for %d minutes. Worked Minutes: %d, Pay Multiplier: %s",
                nightMinutes,
                nightMinutes,
                payMultiplier
        );

        return buildResult("NIGHT_WORKED", true, message);
    }

    /**
     * Calculates the number of minutes worked within the night allowance window.
     * Handles overnight windows correctly.
     */
    private long calculateNightMinutes(LocalDateTime workStart, LocalDateTime workEnd, LocalTime nightStart, LocalTime nightEnd) {
        long totalNightMinutes = 0;
        LocalDateTime current = workStart;

        while (current.isBefore(workEnd)) {
            LocalDateTime next = current.plusMinutes(1);
            if (isWithinNightWindow(current.toLocalTime(), nightStart, nightEnd)) {
                totalNightMinutes++;
            }
            current = next;
        }
        return totalNightMinutes;
    }

    private boolean isWithinNightWindow(LocalTime time, LocalTime nightStart, LocalTime nightEnd) {
        if (nightStart.isBefore(nightEnd)) { // Same-day window (e.g., 01:00 to 05:00)
            return !time.isBefore(nightStart) && time.isBefore(nightEnd);
        } else { // Overnight window (e.g., 22:00 to 06:00)
            return !time.isBefore(nightStart) || time.isBefore(nightEnd);
        }
    }


    /**
     * Calculates the duration of overlap between two time intervals.
     */
    private long calculateOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime overlapStart = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime overlapEnd = end1.isBefore(end2) ? end1 : end2;

        if (overlapStart.isBefore(overlapEnd)) {
            return Duration.between(overlapStart, overlapEnd).toMinutes();
        }
        return 0;
    }

    /**
     * Helper to build the result DTO.
     */
    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(result)
                .success(success)
                .message(message)
                .build();
    }
}
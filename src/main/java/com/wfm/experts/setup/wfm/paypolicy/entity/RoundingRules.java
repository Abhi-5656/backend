//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "rounding_rules")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class RoundingRules implements PayPolicyRule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private boolean enabled;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private RoundingRuleScope scope;
//
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "clock_in_rule_id")
//    private RoundingRule clockIn;
//
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "clock_out_rule_id")
//    private RoundingRule clockOut;
//
//    // --- PayPolicyRule interface implementation ---
//
//    @Override
//    public String getName() {
//        return "RoundingRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        return enabled;
//    }
//
//    private LocalDateTime applyRounding(LocalDateTime original, RoundingRule rule) {
//        if (rule == null || rule.getInterval() == null || rule.getInterval() <= 0) {
//            return original;
//        }
//
//        int interval = rule.getInterval();
//        int minute = original.getMinute();
//        int roundedMinute;
//
//        switch (rule.getType()) {
//            case UP:
//                roundedMinute = ((minute + interval - 1) / interval) * interval;
//                break;
//            case DOWN:
//                roundedMinute = (minute / interval) * interval;
//                break;
//            case NEAREST:
//            default:
//                roundedMinute = ((minute + interval / 2) / interval) * interval;
//                break;
//        }
//
//        // --- NEW GRACE LOGIC ---
//        // If the adjustment from rounding is larger than the grace period,
//        // then the rounding is canceled and we use the original "minute-to-minute" time.
//        Integer grace = rule.getGracePeriod();
//        if (grace != null && Math.abs(roundedMinute - minute) > grace) {
//            return original; // The adjustment is too large, so cancel the rounding.
//        }
//
//        // If no change is needed after rounding and grace check, return original.
//        if (minute == roundedMinute) {
//            return original;
//        }
//
//        // Handle the edge case where rounding up results in the 60th minute,
//        // which should roll over to the next hour.
//        if (roundedMinute == 60) {
//            return original.plusHours(1).withMinute(0).withSecond(0).withNano(0);
//        } else {
//            // Apply the valid rounding.
//            return original.withMinute(roundedMinute).withSecond(0).withNano(0);
//        }
//    }
//
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        if (!evaluate(context)) {
//            return PayPolicyRuleResultDTO.builder()
//                    .ruleName(getName())
//                    .result("NOT_APPLIED")
//                    .success(false)
//                    .message("Rounding not enabled.")
//                    .build();
//        }
//
//        StringBuilder msg = new StringBuilder("Rounding results: ");
//        boolean anyRounded = false;
//
//        for (var punch : context.getPunchEvents()) {
//            boolean shouldRound = false;
//            LocalDateTime original = punch.getEventTime();
//            LocalDateTime rounded = original;
//            String punchType = punch.getPunchType().name();
//
//            boolean isClockIn = "IN".equals(punchType);
//            boolean isClockOut = "OUT".equals(punchType);
//
//            if ((scope == RoundingRuleScope.BOTH || scope == RoundingRuleScope.CLOCK_IN) && isClockIn && clockIn != null) {
//                rounded = applyRounding(original, clockIn);
//                shouldRound = true;
//            }
//
//            if ((scope == RoundingRuleScope.BOTH || scope == RoundingRuleScope.CLOCK_OUT) && isClockOut && clockOut != null) {
//                rounded = applyRounding(original, clockOut);
//                shouldRound = true;
//            }
//
//            if (shouldRound && !rounded.equals(original)) {
//                msg.append("[").append(punchType).append(" punch: ")
//                        .append(original.toLocalTime()).append(" → ")
//                        .append(rounded.toLocalTime()).append("] ");
//                punch.setEventTime(rounded);
//                anyRounded = true;
//            }
//        }
//
//        if (!anyRounded) {
//            msg.append("No punches required rounding.");
//        }
//
//        return PayPolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result(anyRounded ? "ROUNDED" : "NOT_APPLIED")
//                .success(true)
//                .message(msg.toString().trim())
//                .build();
//    }
//}

package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift; // Assuming this is the correct import path
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent; // Assuming this is the correct import path
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "clock_in_rule_id")
    private RoundingRule clockIn;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "clock_out_rule_id")
    private RoundingRule clockOut;

    @Override
    public String getName() {
        return "RoundingRules";
    }

    /**
     * The rule is applicable only if it's enabled and the context contains a valid employee shift.
     */
    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // This ensures the rule only runs if it has the shift data it needs.
        return enabled && context.getFact("shift") instanceof EmployeeShift;
    }

    /**
     * Applies shift-aware rounding to a punch time.
     *
     * @param originalPunch The original punch time.
     * @param scheduledShiftTime The scheduled start/end time of the shift.
     * @param rule The rounding rule to apply (clock-in or clock-out).
     * @return The potentially rounded LocalDateTime.
     */
    private LocalDateTime applyRounding(LocalDateTime originalPunch, LocalDateTime scheduledShiftTime, RoundingRule rule) {
        if (rule == null) {
            return originalPunch;
        }

        // --- 1. CHECK IF PUNCH IS WITHIN THE APPLICATION WINDOW (FIXED LOGIC) ---
        Integer applyBeforeMins = rule.getApplyBeforeShiftMinutes();
        if (applyBeforeMins != null) {
            LocalDateTime windowStart = scheduledShiftTime.minusMinutes(applyBeforeMins);
            if (originalPunch.isBefore(windowStart)) {
                return originalPunch; // Punch is before the window starts, do not round.
            }
        }

        Integer applyAfterMins = rule.getApplyAfterShiftMinutes();
        if (applyAfterMins != null) {
            LocalDateTime windowEnd = scheduledShiftTime.plusMinutes(applyAfterMins);
            if (originalPunch.isAfter(windowEnd)) {
                return originalPunch; // Punch is after the window ends, do not round.
            }
        }

        // --- 2. CHECK FOR GRACE PERIOD ---
        // A grace period punch is moved directly to the scheduled time.
        Integer graceMins = rule.getGracePeriod();
        if (graceMins != null && graceMins > 0) {
            long minutesDifference = ChronoUnit.MINUTES.between(scheduledShiftTime, originalPunch);
            if (Math.abs(minutesDifference) <= graceMins) {
                return scheduledShiftTime; // Inside grace period, snap to schedule.
            }
        }

        // --- 3. APPLY ROUNDING ---
        Integer interval = rule.getInterval();
        if (interval == null || interval <= 0) {
            return originalPunch; // No rounding interval defined.
        }

        int minuteOfHour = originalPunch.getMinute();
        int roundedMinute;

        switch (rule.getType()) {
            case UP:
                roundedMinute = (int) (Math.ceil((double) minuteOfHour / interval) * interval);
                break;
            case DOWN:
                roundedMinute = (int) (Math.floor((double) minuteOfHour / interval) * interval);
                break;
            case NEAREST:
            default:
                roundedMinute = (int) (Math.round((double) minuteOfHour / interval) * interval);
                break;
        }

        // If no change after rounding, return original to avoid unnecessary object creation.
        if (minuteOfHour == roundedMinute) {
            return originalPunch;
        }

        // Handle rollover (e.g., rounding 53 up to 60 with a 15 min interval)
        if (roundedMinute == 60) {
            return originalPunch.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        } else {
            return originalPunch.withMinute(roundedMinute).withSecond(0).withNano(0);
        }
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // The evaluate method already ensures the shift exists.
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        // Additional safeguard in case the shift entity or its nested shift is null
        if (employeeShift == null || employeeShift.getShift() == null) {
            return PayPolicyRuleResultDTO.builder()
                    .ruleName(getName())
                    .result("NO_SHIFT_DATA")
                    .success(false)
                    .message("No valid shift data found in the execution context.")
                    .build();
        }

        StringBuilder msg = new StringBuilder("Rounding results: ");
        boolean anyRounded = false;

        LocalDate shiftDate = employeeShift.getCalendarDate();
        LocalDateTime scheduledClockIn = employeeShift.getShift().getStartTime().atDate(shiftDate);
        LocalDateTime scheduledClockOut = employeeShift.getShift().getEndTime().atDate(shiftDate);

        // Handle overnight shifts for clock-out
        if (scheduledClockOut.isBefore(scheduledClockIn)) {
            scheduledClockOut = scheduledClockOut.plusDays(1);
        }

        // It's good practice to sort punches to process them chronologically
        List<PunchEvent> punches = context.getPunchEvents();
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));

        for (var punch : punches) {
            LocalDateTime originalTime = punch.getEventTime();
            LocalDateTime roundedTime = originalTime;
            String punchType = punch.getPunchType().name();

            boolean isClockIn = "IN".equals(punchType);
            boolean isClockOut = "OUT".equals(punchType);

            if ((scope == RoundingRuleScope.BOTH || scope == RoundingRuleScope.CLOCK_IN) && isClockIn) {
                roundedTime = applyRounding(originalTime, scheduledClockIn, clockIn);
            } else if ((scope == RoundingRuleScope.BOTH || scope == RoundingRuleScope.CLOCK_OUT) && isClockOut) {
                roundedTime = applyRounding(originalTime, scheduledClockOut, clockOut);
            }

            if (!roundedTime.equals(originalTime)) {
                msg.append(String.format("[%s punch: %s → %s] ",
                        punchType,
                        originalTime.toLocalTime(),
                        roundedTime.toLocalTime()));
                punch.setEventTime(roundedTime);
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
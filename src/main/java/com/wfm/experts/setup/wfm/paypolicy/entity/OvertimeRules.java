//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.*;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import com.wfm.experts.setup.wfm.shift.entity.Shift;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Entity
//@Table(name = "overtime_rules")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class OvertimeRules implements PayPolicyRule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private boolean enabled;
//    private Integer thresholdHours;
//    private Integer thresholdMinutes;
//    private Double maxOtPerDay;
//    private Double maxOtPerWeek;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private OvertimeConflictResolution conflictResolution;
//
//    private boolean resetOtBucketDaily;
//    private boolean resetOtBucketWeekly;
//    private boolean resetOtBucketOnPayPeriod;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private CompensationMethod compensationMethod;
//
//    private Double paidOtMultiplier;
//    private Integer compOffDaysPerOt;
//    private Integer compOffHoursPerOt;
//    private Integer maxCompOffBalance;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private CompOffBalanceBasis maxCompOffBalanceBasis;
//
//    private Integer compOffExpiryValue;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 10)
//    private ExpiryUnit compOffExpiryUnit;
//
//    private boolean encashOnExpiry;
//
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "pre_shift_inclusion_id")
//    private PreShiftInclusion preShiftInclusion;
//
//    @ManyToMany(fetch = FetchType.EAGER) // Eager fetch for rule execution
//    @JoinTable(
//            name = "overtime_rules_shifts",
//            joinColumns = @JoinColumn(name = "overtime_rules_id"),
//            inverseJoinColumns = @JoinColumn(name = "shift_id")
//    )
//    private List<Shift> shifts;
//
//
//    // --- Implement PayPolicyRule ---
//
//    @Override
//    public String getName() {
//        return "OvertimeRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        // Rule should only run if it's enabled and there are worked minutes to evaluate
//        return enabled && context.getFact("workedMinutes") != null;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        // 1. Get necessary data from the context
//        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//
//        // 2. Shift Eligibility Check
//        List<Shift> eligibleShifts = getShifts();
//        if (eligibleShifts != null && !eligibleShifts.isEmpty()) {
//            if (employeeShift == null || employeeShift.getShift() == null) {
//                return buildResult("NOT_ELIGIBLE", true, "Overtime not applicable: No shift assigned.");
//            }
//            boolean isEligible = eligibleShifts.stream()
//                    .map(Shift::getId)
//                    .anyMatch(id -> id.equals(employeeShift.getShift().getId()));
//            if (!isEligible) {
//                return buildResult("NOT_ELIGIBLE_SHIFT", true, "Overtime not applicable for the assigned shift.");
//            }
//        }
//
//        // 3. Calculate Overtime Threshold
//        int thresholdInMinutes = (this.thresholdHours != null ? this.thresholdHours * 60 : 0)
//                + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
//
//        if (workedMinutes <= thresholdInMinutes) {
//            context.getFacts().put("overtimeMinutes", 0);
//            return buildResult("NO_OVERTIME", true, "Work duration does not exceed overtime threshold.");
//        }
//
//        // 4. Calculate daily overtime
//        int calculatedOvertime = workedMinutes - thresholdInMinutes;
//
//        // 5. Apply daily cap
//        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
//            int maxDailyMinutes = (int) (this.maxOtPerDay * 60);
//            if (calculatedOvertime > maxDailyMinutes) {
//                calculatedOvertime = maxDailyMinutes;
//            }
//        }
//
//        // Note: Weekly and Pay Period caps would require historical data not present in the current context.
//        // This would be a future enhancement requiring fetching weekly timesheet data.
//
//        // 6. Update the context with the calculated overtime
//        context.getFacts().put("overtimeMinutes", calculatedOvertime);
//
//        return buildResult("OVERTIME_CALCULATED", true, "Calculated " + calculatedOvertime + " minutes of overtime.");
//    }
//
//    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
//        return PayPolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result(result)
//                .success(success)
//                .message(message)
//                .build();
//    }
//}
package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "overtime_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRules implements PayPolicyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private Integer thresholdHours;
    private Integer thresholdMinutes;
    private Double maxOtPerDay;
    private Double maxOtPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OvertimeConflictResolution conflictResolution;

    private boolean resetOtBucketDaily;
    private boolean resetOtBucketWeekly;
    private boolean resetOtBucketOnPayPeriod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompensationMethod compensationMethod;

    private Double paidOtMultiplier;
    private Integer compOffDaysPerOt;
    private Integer compOffHoursPerOt;
    private Integer maxCompOffBalance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompOffBalanceBasis maxCompOffBalanceBasis;

    private Integer compOffExpiryValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ExpiryUnit compOffExpiryUnit;

    private boolean encashOnExpiry;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pre_shift_inclusion_id")
    private PreShiftInclusion preShiftInclusion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "overtime_rules_shifts",
            joinColumns = @JoinColumn(name = "overtime_rules_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    private List<Shift> shifts;

    @Enumerated(EnumType.STRING)
    private DailyOtTrigger dailyOtTrigger;

    private Integer gracePeriodAfterShiftEnd;

    private boolean enableWeeklyOt;

    private Integer weeklyThresholdHours;

    @Enumerated(EnumType.STRING)
    private WeeklyOtBasis weeklyOtBasis;

    @Enumerated(EnumType.STRING)
    private DailyWeeklyOtConflict dailyWeeklyOtConflict;

    @Enumerated(EnumType.STRING)
    private WeekDay weeklyResetDay;

    @Override
    public String getName() {
        return "OvertimeRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        LocalDate workDate = context.getDate();
        String employeeId = context.getEmployeeId();

        int dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift, context);
        int weeklyOtMinutes = 0;
        if (enableWeeklyOt) {
            weeklyOtMinutes = calculateWeeklyOvertime(employeeId, workDate, context);
        }

        int totalOvertime = dailyOtMinutes + weeklyOtMinutes;

        context.getFacts().put("overtimeMinutes", totalOvertime);

        return buildResult("OVERTIME_CALCULATED", true, "Overtime calculated (Daily: " + dailyOtMinutes + ", Weekly: " + weeklyOtMinutes + ").");
    }

    private int calculateDailyOvertime(Integer workedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        List<Shift> eligibleShifts = getShifts();
        if (eligibleShifts != null && !eligibleShifts.isEmpty()) {
            if (employeeShift == null || employeeShift.getShift() == null || !isShiftEligible(employeeShift.getShift(), eligibleShifts)) {
                return 0;
            }
        }

        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift, context);
        int overtimeEligibleMinutes = getOvertimeEligibleMinutes(workedMinutes, employeeShift, context);

        if (overtimeEligibleMinutes <= thresholdInMinutes) {
            return 0;
        }

        int calculatedDailyOt = overtimeEligibleMinutes - thresholdInMinutes;

        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            calculatedDailyOt = Math.min(calculatedDailyOt, (int) (this.maxOtPerDay * 60));
        }

        return calculatedDailyOt;
    }

    private int calculateWeeklyOvertime(String employeeId, LocalDate workDate, PayPolicyExecutionContext context) {
        if (weeklyThresholdHours == null || weeklyThresholdHours <= 0) {
            return 0;
        }

        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
        Integer dailyOtMinutes = (Integer) context.getFacts().getOrDefault("dailyOtMinutes", 0);


        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.SUNDAY;
        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
        if (workDate.isBefore(weekStartDate)) {
            weekStartDate = weekStartDate.minusWeeks(1);
        }

        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));

        int weeklyTotalMinutes = 0;
        for (Timesheet sheet : pastWeekTimesheets) {
            weeklyTotalMinutes += getMinutesForWeeklyBasis(sheet);
        }

        Integer currentDayWorkedMinutes = context.getWorkedMinutes();
        if (currentDayWorkedMinutes != null) {
            if (this.weeklyOtBasis == WeeklyOtBasis.REGULAR_HOURS_ONLY) {
                weeklyTotalMinutes += (currentDayWorkedMinutes - dailyOtMinutes);
            } else {
                weeklyTotalMinutes += currentDayWorkedMinutes;
            }
        }

        int weeklyThresholdMinutes = weeklyThresholdHours * 60;
        if (weeklyTotalMinutes <= weeklyThresholdMinutes) {
            return 0;
        }

        int potentialWeeklyOt = weeklyTotalMinutes - weeklyThresholdMinutes;

        int totalDailyOtInWeek = pastWeekTimesheets.stream()
                .mapToInt(sheet -> sheet.getOvertimeDuration() != null ? sheet.getOvertimeDuration() : 0)
                .sum();


        int alreadyAccruedWeeklyOt = totalDailyOtInWeek;

        if (this.maxOtPerWeek != null && this.maxOtPerWeek > 0) {
            int maxWeeklyOtMinutes = (int) (this.maxOtPerWeek * 60);
            int remainingWeeklyOtAllowed = maxWeeklyOtMinutes - alreadyAccruedWeeklyOt;
            potentialWeeklyOt = Math.min(potentialWeeklyOt, remainingWeeklyOtAllowed);
        }

        return Math.max(0, potentialWeeklyOt);
    }

    private int getMinutesForWeeklyBasis(Timesheet sheet) {
        int workDuration = sheet.getWorkDurationMinutes() != null ? sheet.getWorkDurationMinutes() : 0;
        int overtimeDuration = sheet.getOvertimeDuration() != null ? sheet.getOvertimeDuration() : 0;

        if (this.weeklyOtBasis == WeeklyOtBasis.REGULAR_HOURS_ONLY) {
            return workDuration - overtimeDuration;
        }
        return workDuration;
    }


    private boolean isShiftEligible(Shift currentShift, List<Shift> eligibleShifts) {
        return eligibleShifts.stream().anyMatch(s -> s.getId().equals(currentShift.getId()));
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            LocalDateTime shiftEnd = employeeShift.getShift().getEndTime().atDate(employeeShift.getCalendarDate());
            if (shiftEnd.isBefore(employeeShift.getShift().getStartTime().atDate(employeeShift.getCalendarDate()))) {
                shiftEnd = shiftEnd.plusDays(1);
            }
            LocalDateTime graceEnd = shiftEnd.plusMinutes(this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);

            Optional<PunchEvent> lastOutPunchOpt = context.getPunchEvents().stream()
                    .filter(p -> p.getPunchType() == PunchType.OUT)
                    .max(Comparator.comparing(PunchEvent::getEventTime));

            if (lastOutPunchOpt.isPresent() && lastOutPunchOpt.get().getEventTime().isAfter(graceEnd)) {
                return (int) Duration.between(graceEnd, lastOutPunchOpt.get().getEventTime()).toMinutes();
            } else {
                return 0;
            }
        }
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
    }

    private int getOvertimeEligibleMinutes(Integer workedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        int overtimeEligibleMinutes = workedMinutes;
        if (preShiftInclusion != null && preShiftInclusion.isEnabled() && employeeShift != null && employeeShift.getShift() != null) {
            final int[] minutesToDeduct = {0};
            context.getPunchEvents().stream()
                    .filter(p -> p.getPunchType() == PunchType.IN)
                    .min(Comparator.comparing(PunchEvent::getEventTime))
                    .ifPresent(firstIn -> {
                        LocalDateTime shiftStartTime = employeeShift.getShift().getStartTime().atDate(employeeShift.getCalendarDate());
                        LocalDateTime punchInTime = firstIn.getEventTime();

                        if (punchInTime.isBefore(shiftStartTime)) {
                            long actualMinutesBeforeShift = Duration.between(punchInTime, shiftStartTime).toMinutes();
                            int allowedInclusionMinutes = 0;
                            if (preShiftInclusion.getFromValue() != null) {
                                allowedInclusionMinutes = "hours".equalsIgnoreCase(preShiftInclusion.getFromUnit()) ?
                                        preShiftInclusion.getFromValue() * 60 : preShiftInclusion.getFromValue();
                            }
                            if (actualMinutesBeforeShift > allowedInclusionMinutes) {
                                minutesToDeduct[0] = (int) (actualMinutesBeforeShift - allowedInclusionMinutes);
                            }
                        }
                    });
            overtimeEligibleMinutes -= minutesToDeduct[0];
        }
        return overtimeEligibleMinutes;
    }

    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(result)
                .success(success)
                .message(message)
                .build();
    }
}
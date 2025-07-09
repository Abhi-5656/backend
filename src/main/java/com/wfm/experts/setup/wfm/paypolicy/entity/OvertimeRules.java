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

        // 1. Calculate this day's Daily OT based on the total worked minutes.
        int dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift);

        // 2. The remaining hours are considered "regular" for the purpose of the weekly calculation.
        int regularHoursForWeeklyCalc = workedMinutes - dailyOtMinutes;

        // 3. Calculate Weekly OT based on the sum of regular hours for the entire week.
        int weeklyOtMinutes = 0;
        if (enableWeeklyOt) {
            weeklyOtMinutes = calculateWeeklyOvertime(employeeId, workDate, context, regularHoursForWeeklyCalc);
        }

        // 4. Final Regular Hours for the day is the portion that is NOT daily OT or weekly OT.
        int finalRegularHours = workedMinutes - dailyOtMinutes - weeklyOtMinutes;

        // 5. Handle excess hours based on the daily OT calculation.
        int excessHoursMinutes = 0;
        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            int maxDailyOtMinutes = (int) (this.maxOtPerDay * 60);
            if (dailyOtMinutes > maxDailyOtMinutes) {
                excessHoursMinutes = dailyOtMinutes - maxDailyOtMinutes;
                dailyOtMinutes = maxDailyOtMinutes;
            }
        }

        context.getFacts().put("regularHoursMinutes", finalRegularHours);
        context.getFacts().put("dailyOtHoursMinutes", dailyOtMinutes);
        context.getFacts().put("excessHoursMinutes", excessHoursMinutes);
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOtMinutes);


        return buildResult("OVERTIME_CALCULATED", true, "Overtime calculated (Daily: " + dailyOtMinutes + ", Weekly: " + weeklyOtMinutes + ", Excess: " + excessHoursMinutes + ").");
    }

    private int calculateDailyOvertime(Integer workedMinutes, EmployeeShift employeeShift) {
        // If there's no shift, we check if OT is triggered by fixed hours.
        if (employeeShift == null || employeeShift.getShift() == null) {
            if(dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
                int threshold = (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
                if(workedMinutes > threshold) {
                    return workedMinutes - threshold;
                }
            }
            return 0; // No shift and no fixed hour trigger means no daily OT.
        }

        // Check if the assigned shift is eligible for OT.
        List<Shift> eligibleShifts = getShifts();
        if (eligibleShifts != null && !eligibleShifts.isEmpty() && !isShiftEligible(employeeShift.getShift(), eligibleShifts)) {
            return 0;
        }

        // Calculate the threshold for daily OT.
        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift);
        if (workedMinutes <= thresholdInMinutes) {
            return 0;
        }

        return workedMinutes - thresholdInMinutes;
    }

    private int calculateWeeklyOvertime(String employeeId, LocalDate workDate, PayPolicyExecutionContext context, int currentDayRegularMinutes) {
        if (weeklyThresholdHours == null || weeklyThresholdHours <= 0) {
            return 0;
        }

        TimesheetRepository timesheetRepository = context.getTimesheetRepository();

        // Determine the start of the current work week.
        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.SUNDAY;
        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
        if (workDate.isBefore(weekStartDate)) {
            weekStartDate = weekStartDate.minusWeeks(1);
        }

        // Get all timesheets from the start of the week up to yesterday.
        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));

        // Sum up the REGULAR hours from the past days of the week.
        int pastRegularMinutes = pastWeekTimesheets.stream()
                .mapToInt(sheet -> {
                    int total = sheet.getTotalWorkDurationMinutes() != null ? sheet.getTotalWorkDurationMinutes() : 0;
                    int dailyOt = sheet.getDailyOtHoursMinutes() != null ? sheet.getDailyOtHoursMinutes() : 0;
                    return total - dailyOt; // Calculate past regular hours
                })
                .sum();

        int totalRegularMinutesThisWeek = pastRegularMinutes + currentDayRegularMinutes;
        int weeklyThresholdMinutes = weeklyThresholdHours * 60;

        if (totalRegularMinutesThisWeek <= weeklyThresholdMinutes) {
            return 0;
        }

        // The total amount of weekly OT earned for the whole week so far.
        int totalWeeklyOtSoFar = totalRegularMinutesThisWeek - weeklyThresholdMinutes;

        // Weekly OT already accounted for in previous days.
        int pastWeeklyOt = pastWeekTimesheets.stream()
                .mapToInt(sheet -> sheet.getWeeklyOtHoursMinutes() != null ? sheet.getWeeklyOtHoursMinutes() : 0)
                .sum();

        // The new weekly OT for today is the total so far, minus what was already logged.
        int weeklyOtForToday = totalWeeklyOtSoFar - pastWeeklyOt;

        return Math.max(0, weeklyOtForToday);
    }

    private int getMinutesForWeeklyBasis(Timesheet sheet) {
        int workDuration = sheet.getTotalWorkDurationMinutes() != null ? sheet.getTotalWorkDurationMinutes() : 0;
        int overtimeDuration = sheet.getDailyOtHoursMinutes() != null ? sheet.getDailyOtHoursMinutes() : 0;

        if (this.weeklyOtBasis == WeeklyOtBasis.REGULAR_HOURS_ONLY) {
            return workDuration - overtimeDuration;
        }
        return workDuration;
    }


    private boolean isShiftEligible(Shift currentShift, List<Shift> eligibleShifts) {
        return eligibleShifts.stream().anyMatch(s -> s.getId().equals(currentShift.getId()));
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END) {
            int shiftDuration = (int) Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440; // overnight shift
            }
            return shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
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
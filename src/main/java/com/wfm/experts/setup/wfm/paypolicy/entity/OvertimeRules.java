//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.*;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import com.wfm.experts.setup.wfm.shift.entity.Shift;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.DayOfWeek;
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Optional;
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
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "overtime_rules_shifts",
//            joinColumns = @JoinColumn(name = "overtime_rules_id"),
//            inverseJoinColumns = @JoinColumn(name = "shift_id")
//    )
//    private List<Shift> shifts;
//
//    @Enumerated(EnumType.STRING)
//    private DailyOtTrigger dailyOtTrigger;
//
//    private Integer gracePeriodAfterShiftEnd;
//
//    private boolean enableWeeklyOt;
//
//    private Integer weeklyThresholdHours;
//
//    @Enumerated(EnumType.STRING)
//    private WeeklyOtBasis weeklyOtBasis;
//
//    @Enumerated(EnumType.STRING)
//    private DailyWeeklyOtConflict dailyWeeklyOtConflict;
//
//    @Enumerated(EnumType.STRING)
//    private WeekDay weeklyResetDay;
//
//    @Override
//    public String getName() {
//        return "OvertimeRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        return enabled && context.getFact("workedMinutes") != null;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//        LocalDate workDate = context.getDate();
//        String employeeId = context.getEmployeeId();
//
//        int dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift, context);
//        int weeklyOtMinutes = 0;
//
//        if (enableWeeklyOt) {
//            int regularForWeekly = workedMinutes - dailyOtMinutes;
//            weeklyOtMinutes = calculateWeeklyOvertime(employeeId, workDate, context, regularForWeekly);
//        }
//
//        int finalRegularHours = workedMinutes - dailyOtMinutes - weeklyOtMinutes;
//        int excessHoursMinutes = 0;
//
//        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
//            int maxDailyOt = (int) (this.maxOtPerDay * 60);
//            if (dailyOtMinutes > maxDailyOt) {
//                excessHoursMinutes = dailyOtMinutes - maxDailyOt;
//                dailyOtMinutes = maxDailyOt;
//            }
//        }
//
//        context.getFacts().put("regularHoursMinutes", finalRegularHours);
//        context.getFacts().put("dailyOtHoursMinutes", dailyOtMinutes);
//        context.getFacts().put("weeklyOtHoursMinutes", weeklyOtMinutes);
//        context.getFacts().put("excessHoursMinutes", excessHoursMinutes);
//
//        return buildResult("OVERTIME_CALCULATED", true, "Overtime rule executed. Daily OT: " + dailyOtMinutes + ", Weekly OT: " + weeklyOtMinutes);
//    }
//
//    private int calculateDailyOvertime(Integer workedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
//        if (workedMinutes == null || workedMinutes <= 0) {
//            return 0;
//        }
//
//        int thresholdInMinutes;
//
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END) {
//            if (employeeShift == null || employeeShift.getShift() == null) {
//                return 0;
//            }
//            List<Shift> eligibleShifts = getShifts();
//            if (eligibleShifts != null && !eligibleShifts.isEmpty() && !isShiftEligible(employeeShift.getShift(), eligibleShifts)) {
//                return 0;
//            }
//            thresholdInMinutes = getDailyThresholdInMinutes(employeeShift, context);
//
//        } else if (dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
//            if ((this.thresholdHours == null || this.thresholdHours <= 0) && (this.thresholdMinutes == null || this.thresholdMinutes <= 0)) {
//                return 0;
//            }
//            thresholdInMinutes = getDailyThresholdInMinutes(null, context);
//        } else {
//            return 0;
//        }
//
//        if (workedMinutes <= thresholdInMinutes) {
//            return 0;
//        }
//
//        return workedMinutes - thresholdInMinutes;
//    }
//
//    private int calculateWeeklyOvertime(String employeeId, LocalDate workDate, PayPolicyExecutionContext context, int currentDayRegularMinutes) {
//        if (weeklyThresholdHours == null || weeklyThresholdHours <= 0) {
//            return 0;
//        }
//
//        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
//
//        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.SUNDAY;
//        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
//        if (workDate.isBefore(weekStartDate)) {
//            weekStartDate = weekStartDate.minusWeeks(1);
//        }
//
//        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
//
//        int pastRegularMinutes = pastWeekTimesheets.stream()
//                .mapToInt(sheet -> {
//                    int total = sheet.getTotalWorkDurationMinutes() != null ? sheet.getTotalWorkDurationMinutes() : 0;
//                    return total;
//                })
//                .sum();
//
//        int totalRegularMinutesThisWeek = pastRegularMinutes + currentDayRegularMinutes;
//        int weeklyThresholdMinutes = weeklyThresholdHours * 60;
//
//        if (totalRegularMinutesThisWeek <= weeklyThresholdMinutes) {
//            return 0;
//        }
//
//        int totalWeeklyOtSoFar = totalRegularMinutesThisWeek - weeklyThresholdMinutes;
//        int pastWeeklyOt = pastWeekTimesheets.stream()
//                .mapToInt(sheet -> 0)
//                .sum();
//        int weeklyOtForToday = totalWeeklyOtSoFar - pastWeeklyOt;
//
//        return Math.max(0, weeklyOtForToday);
//    }
//
//    private boolean isShiftEligible(Shift currentShift, List<Shift> eligibleShifts) {
//        return eligibleShifts.stream().anyMatch(s -> s.getId().equals(currentShift.getId()));
//    }
//
//    private int getDailyThresholdInMinutes(EmployeeShift employeeShift, PayPolicyExecutionContext context) {
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) {
//                shiftDuration += 1440;
//            }
//            Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
//            long payableShiftDuration = shiftDuration - unpaidBreakMinutes;
//            return (int) payableShiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
//        }
//        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
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
//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.*;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import com.wfm.experts.setup.wfm.shift.entity.Shift;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.extern.slf4j.Slf4j;
//
//import java.time.DayOfWeek;
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
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
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "overtime_rules_shifts",
//            joinColumns = @JoinColumn(name = "overtime_rules_id"),
//            inverseJoinColumns = @JoinColumn(name = "shift_id")
//    )
//    private List<Shift> shifts;
//
//    @Enumerated(EnumType.STRING)
//    private DailyOtTrigger dailyOtTrigger;
//
//    private Integer gracePeriodAfterShiftEnd;
//
//    private boolean enableWeeklyOt;
//
//    private Integer weeklyThresholdHours;
//
//    @Enumerated(EnumType.STRING)
//    private WeeklyOtBasis weeklyOtBasis;
//
//    @Enumerated(EnumType.STRING)
//    private DailyWeeklyOtConflict dailyWeeklyOtConflict;
//
//    @Enumerated(EnumType.STRING)
//    private WeekDay weeklyResetDay;
//
//    @Override
//    public String getName() {
//        return "OvertimeRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        return enabled && context.getFact("workedMinutes") != null;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        // 1. Get the final payable time (after break deductions).
//        Integer netPayableMinutes = (Integer) context.getFact("workedMinutes");
//        if (netPayableMinutes == null || netPayableMinutes <= 0) {
//            return buildResult("NO_WORK_TIME", true, "No payable work time to process.");
//        }
//
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//
//        // 2. Calculate Overtime based on the configured threshold.
//        int dailyOtMinutes = calculateDailyOvertime(netPayableMinutes, employeeShift, context);
//
//        // 3. Handle Weekly Overtime if enabled.
//        int weeklyOtMinutes = 0;
//        if (enableWeeklyOt) {
//            int regularForWeekly = netPayableMinutes - dailyOtMinutes;
//            weeklyOtMinutes = calculateWeeklyOvertime(context.getEmployeeId(), context.getDate(), context, regularForWeekly);
//        }
//
//        // 4. Determine the Regular Hours Cap based on the shift schedule.
//        long payableShiftDuration = netPayableMinutes; // Default to all time being regular if no shift
//        if (employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) shiftDuration += 1440;
//            Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
//            payableShiftDuration = shiftDuration - unpaidBreakMinutes;
//        }
//
//        // 5. Split the remaining time into Regular and Excess.
//        int remainingMinutes = netPayableMinutes - dailyOtMinutes - weeklyOtMinutes;
//        int regularMinutes = (int) Math.min(remainingMinutes, payableShiftDuration);
//        int excessHoursMinutes = remainingMinutes - regularMinutes;
//
//        // 6. Handle Max OT per day rule (move excess from OT to Excess Hours).
//        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
//            int maxDailyOt = (int) (this.maxOtPerDay * 60);
//            if (dailyOtMinutes > maxDailyOt) {
//                excessHoursMinutes += (dailyOtMinutes - maxDailyOt); // Add the capped amount to excess
//                dailyOtMinutes = maxDailyOt;
//            }
//        }
//
//        // 7. Update the context with the final, authoritative values.
//        context.getFacts().put("regularHoursMinutes", Math.max(0, regularMinutes));
//        context.getFacts().put("dailyOtHoursMinutes", Math.max(0, dailyOtMinutes));
//        context.getFacts().put("weeklyOtHoursMinutes", Math.max(0, weeklyOtMinutes));
//        context.getFacts().put("excessHoursMinutes", Math.max(0, excessHoursMinutes));
//
//        String message = String.format("Overtime rule executed. Regular: %d, Daily OT: %d, Weekly OT: %d, Excess: %d",
//                regularMinutes, dailyOtMinutes, weeklyOtMinutes, excessHoursMinutes);
//
//        return buildResult("OVERTIME_CALCULATED", true, message);
//    }
//
//    private int calculateDailyOvertime(Integer workedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
//        if (workedMinutes == null || workedMinutes <= 0) {
//            return 0;
//        }
//
//        int thresholdInMinutes;
//
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END) {
//            if (employeeShift == null || employeeShift.getShift() == null) {
//                return 0;
//            }
//            List<Shift> eligibleShifts = getShifts();
//            if (eligibleShifts != null && !eligibleShifts.isEmpty() && !isShiftEligible(employeeShift.getShift(), eligibleShifts)) {
//                return 0;
//            }
//            thresholdInMinutes = getDailyThresholdInMinutes(employeeShift, context);
//
//        } else if (dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
//            if ((this.thresholdHours == null || this.thresholdHours <= 0) && (this.thresholdMinutes == null || this.thresholdMinutes <= 0)) {
//                return 0;
//            }
//            thresholdInMinutes = getDailyThresholdInMinutes(null, context);
//        } else {
//            return 0;
//        }
//
//        if (workedMinutes <= thresholdInMinutes) {
//            return 0;
//        }
//
//        return workedMinutes - thresholdInMinutes;
//    }
//
//    private int calculateWeeklyOvertime(String employeeId, LocalDate workDate, PayPolicyExecutionContext context, int currentDayRegularMinutes) {
//        if (weeklyThresholdHours == null || weeklyThresholdHours <= 0) {
//            return 0;
//        }
//
//        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
//
//        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.SUNDAY;
//        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
//        if (workDate.isBefore(weekStartDate)) {
//            weekStartDate = weekStartDate.minusWeeks(1);
//        }
//
//        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
//
//        int pastRegularMinutes = pastWeekTimesheets.stream()
//                .mapToInt(sheet -> sheet.getRegularHoursMinutes() != null ? sheet.getRegularHoursMinutes() : 0)
//                .sum();
//
//        int totalRegularMinutesThisWeek = pastRegularMinutes + currentDayRegularMinutes;
//        int weeklyThresholdMinutes = weeklyThresholdHours * 60;
//
//        if (totalRegularMinutesThisWeek <= weeklyThresholdMinutes) {
//            return 0;
//        }
//
//        int totalWeeklyOtSoFar = totalRegularMinutesThisWeek - weeklyThresholdMinutes;
//        int pastWeeklyOt = pastWeekTimesheets.stream()
//                .mapToInt(sheet -> 0) // Simplified - assumes weekly OT is calculated once.
//                .sum();
//        int weeklyOtForToday = totalWeeklyOtSoFar - pastWeeklyOt;
//
//        return Math.max(0, weeklyOtForToday);
//    }
//
//    private boolean isShiftEligible(Shift currentShift, List<Shift> eligibleShifts) {
//        return eligibleShifts.stream().anyMatch(s -> s.getId().equals(currentShift.getId()));
//    }
//
//    private int getDailyThresholdInMinutes(EmployeeShift employeeShift, PayPolicyExecutionContext context) {
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) {
//                shiftDuration += 1440;
//            }
//            Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
//            long payableShiftDuration = shiftDuration - unpaidBreakMinutes;
//            return (int) payableShiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
//        }
//        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
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
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
        // This rule's ONLY job is to identify and report overtime hours.
        // It does NOT calculate the final regular or excess hours.
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        // 1. Calculate Daily Overtime
        int dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift, context);

        // 2. Calculate Weekly Overtime
        int weeklyOtMinutes = 0;
        if (enableWeeklyOt) {
            int nonDailyOtTime = workedMinutes - dailyOtMinutes;
            weeklyOtMinutes = calculateWeeklyOvertime(context.getEmployeeId(), context.getDate(), context, nonDailyOtTime);
        }

        // 3. Apply the "Max OT Per Day" cap if it's configured.
        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            int maxDailyOt = (int) (this.maxOtPerDay * 60);
            if (dailyOtMinutes > maxDailyOt) {
                dailyOtMinutes = maxDailyOt;
            }
        }

        // 4. Put the calculated OT values into the context for the final calculation step.
        context.getFacts().put("dailyOtHoursMinutes", dailyOtMinutes);
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOtMinutes);

        String message = "Overtime rule executed. Identified Daily OT: " + dailyOtMinutes + ", Weekly OT: " + weeklyOtMinutes;
        return buildResult("OT_IDENTIFIED", true, message);
    }

    private int calculateDailyOvertime(Integer workedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        if (workedMinutes == null || workedMinutes <= 0) {
            return 0;
        }

        int thresholdInMinutes;

        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END) {
            if (employeeShift == null || employeeShift.getShift() == null) {
                return 0;
            }
            List<Shift> eligibleShifts = getShifts();
            if (eligibleShifts != null && !eligibleShifts.isEmpty() && !isShiftEligible(employeeShift.getShift(), eligibleShifts)) {
                return 0;
            }
            thresholdInMinutes = getDailyThresholdInMinutes(employeeShift, context);

        } else if (dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
            if ((this.thresholdHours == null || this.thresholdHours <= 0) && (this.thresholdMinutes == null || this.thresholdMinutes <= 0)) {
                return 0;
            }
            thresholdInMinutes = getDailyThresholdInMinutes(null, context);
        } else {
            return 0;
        }

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

        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.SUNDAY;
        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
        if (workDate.isBefore(weekStartDate)) {
            weekStartDate = weekStartDate.minusWeeks(1);
        }

        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));

        int pastRegularMinutes = pastWeekTimesheets.stream()
                .mapToInt(sheet -> sheet.getRegularHoursMinutes() != null ? sheet.getRegularHoursMinutes() : 0)
                .sum();

        int totalRegularMinutesThisWeek = pastRegularMinutes + currentDayRegularMinutes;
        int weeklyThresholdMinutes = weeklyThresholdHours * 60;

        if (totalRegularMinutesThisWeek <= weeklyThresholdMinutes) {
            return 0;
        }

        int totalWeeklyOtSoFar = totalRegularMinutesThisWeek - weeklyThresholdMinutes;

        int pastWeeklyOt = 0; // Simplified for this context.

        int weeklyOtForToday = totalWeeklyOtSoFar - pastWeeklyOt;

        return Math.max(0, weeklyOtForToday);
    }

    private boolean isShiftEligible(Shift currentShift, List<Shift> eligibleShifts) {
        return eligibleShifts.stream().anyMatch(s -> s.getId().equals(currentShift.getId()));
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440;
            }
            Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
            long payableShiftDuration = shiftDuration - unpaidBreakMinutes;
            return (int) payableShiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
        }
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
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
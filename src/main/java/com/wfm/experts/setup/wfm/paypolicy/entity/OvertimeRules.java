
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
    private boolean enableDailyOt;
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
        // This rule runs if enabled and there is gross work time to evaluate.
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // This rule's ONLY job is to identify and report overtime hours.
        // It does NOT calculate the final regular or excess hours.
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        if (workedMinutes == null || workedMinutes <= 0) {
            return buildResult("NO_WORK_TIME", true, "No gross work time to process for overtime.");
        }

        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        // 1. Calculate Daily Overtime based on the GROSS duration.
        int dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift, context);

        // 2. Handle Max OT Per Day cap if configured.
        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            int maxDailyOt = (int) (this.maxOtPerDay * 60);
            if (dailyOtMinutes > maxDailyOt) {
                dailyOtMinutes = maxDailyOt;
            }
        }

        // 3. Put ONLY the calculated OT value into the context.
        context.getFacts().put("dailyOtHoursMinutes", dailyOtMinutes);

        // 4. Weekly OT logic can remain here as it depends on the daily OT calculation.
        int weeklyOtMinutes = 0;
        if (enableWeeklyOt) {
            // **FIX: Use regular hours for weekly OT calculation.**
            int regularHoursForToday = workedMinutes - dailyOtMinutes;
            weeklyOtMinutes = calculateWeeklyOvertime(context.getEmployeeId(), context.getDate(), context, regularHoursForToday);
        }
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOtMinutes);


        String message = "Overtime rule executed on gross time. Identified Daily OT: " + dailyOtMinutes;
        return buildResult("OT_IDENTIFIED", true, message);
    }

    private int calculateDailyOvertime(Integer grossWorkedMinutes, EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
            return 0;
        }

        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift, context);

        if (grossWorkedMinutes <= thresholdInMinutes) {
            return 0;
        }

        return grossWorkedMinutes - thresholdInMinutes;
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift, PayPolicyExecutionContext context) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440;
            }
            // NOTE: Break deduction is no longer needed here for the threshold calculation.
            // The break is deducted from the regular hours portion later.
            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
        }
        // For AFTER_FIXED_HOURS, simply return the configured threshold.
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
    }

    private int calculateWeeklyOvertime(String employeeId, LocalDate workDate, PayPolicyExecutionContext context, int currentDayRegularMinutes) {
        // This logic remains the same.
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
        int pastWeeklyOt = 0;
        int weeklyOtForToday = totalWeeklyOtSoFar - pastWeeklyOt;
        return Math.max(0, weeklyOtForToday);
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
package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        return enabled && (enableDailyOt || enableWeeklyOt) && (workedMinutes != null && workedMinutes > 0 || (Integer) context.getFact("grossWorkMinutes") > 0);
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        if (enableDailyOt) {
            if (employeeShift != null && dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END) {
                executeScheduledOt(context);
            } else if (employeeShift == null && dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
                executeFixedDurationOt(context);
            }
        }

        if (enableWeeklyOt) {
            executeWeeklyOt(context);
        }

        int dailyOt = (int) context.getFacts().getOrDefault("dailyOtHoursMinutes", 0);
        int weeklyOt = (int) context.getFacts().getOrDefault("weeklyOtHoursMinutes", 0);
        int excessOt = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);

        String message = String.format("OT Calculated. Daily: %d, Weekly: %d, Excess: %d.", dailyOt, weeklyOt, excessOt);
        return buildResult("OT_CALCULATED", true, message);
    }

    private void executeScheduledOt(PayPolicyExecutionContext context) {
        int minutesForOtCalculation = (int) context.getFacts().getOrDefault("workedMinutes", 0);
        int dailyOt = 0;
        int excessOt = 0;

        if (minutesForOtCalculation > 0) {
            dailyOt = minutesForOtCalculation;

            if (maxOtPerDay != null && maxOtPerDay > 0) {
                int maxOtInMinutes = (int) (maxOtPerDay * 60);
                if (dailyOt > maxOtInMinutes) {
                    excessOt = dailyOt - maxOtInMinutes;
                    dailyOt = maxOtInMinutes;
                }
            }
        }

        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("excessHoursMinutes", excessOt);
        context.getFacts().put("workedMinutes", 0);
    }

    private void executeFixedDurationOt(PayPolicyExecutionContext context) {
        int minutesForOtCalculation = (int) context.getFacts().getOrDefault("workedMinutes", 0);
        int dailyOt = minutesForOtCalculation;
        int excessOt = 0;

        if (maxOtPerDay != null && maxOtPerDay > 0) {
            int maxOtInMinutes = (int) (maxOtPerDay * 60);
            if (dailyOt > maxOtInMinutes) {
                excessOt = dailyOt - maxOtInMinutes;
                dailyOt = maxOtInMinutes;
            }
        }

        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("excessHoursMinutes", excessOt);
        context.getFacts().put("workedMinutes", 0);
    }

    private void executeWeeklyOt(PayPolicyExecutionContext context) {
        DayOfWeek resetDay = (this.weeklyResetDay != null) ? DayOfWeek.valueOf(this.weeklyResetDay.name()) : DayOfWeek.MONDAY;
        LocalDate today = context.getDate();
        LocalDate weekStart = today.with(resetDay);
        if (today.isBefore(weekStart)) {
            weekStart = weekStart.minusWeeks(1);
        }

        LocalDate weekEndForQuery = today.minusDays(1);

        TimesheetRepository timesheetRepo = context.getTimesheetRepository();

        List<Timesheet> timesheetsThisWeek = timesheetRepo.findByEmployeeIdAndWorkDateBetween(context.getEmployeeId(), weekStart, weekEndForQuery);

        int historicalRegularMinutes = timesheetsThisWeek.stream()
                .mapToInt(ts -> ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0)
                .sum();

        int currentDayRegularMinutes = (int) context.getFacts().getOrDefault("finalRegularMinutes", 0);
        int accumulatedRegularMinutes = historicalRegularMinutes + currentDayRegularMinutes;

        int weeklyThresholdInMinutes = (this.weeklyThresholdHours != null ? this.weeklyThresholdHours : 0) * 60;

        if (accumulatedRegularMinutes > weeklyThresholdInMinutes) {
            int minutesOverThreshold = accumulatedRegularMinutes - weeklyThresholdInMinutes;
            int weeklyOt = Math.min(minutesOverThreshold, currentDayRegularMinutes);
            int excessOt = 0;

            int newRegularMinutes = currentDayRegularMinutes - weeklyOt;
            context.getFacts().put("finalRegularMinutes", newRegularMinutes);

            if (maxOtPerWeek != null && maxOtPerWeek > 0) {
                int maxWeeklyOtInMinutes = (int) (maxOtPerWeek * 60);
                if (weeklyOt > maxWeeklyOtInMinutes) {
                    excessOt = weeklyOt - maxWeeklyOtInMinutes;
                    weeklyOt = maxWeeklyOtInMinutes;
                }
            }

            context.getFacts().put("weeklyOtHoursMinutes", weeklyOt);
            context.getFacts().put("excessHoursMinutes", (int) context.getFacts().getOrDefault("excessHoursMinutes", 0) + excessOt);

            if (dailyWeeklyOtConflict != null) {
                switch (dailyWeeklyOtConflict) {
                    case EXCLUDE_DAILY_OT:
                        int dailyOt = (int) context.getFacts().getOrDefault("dailyOtHoursMinutes", 0);
                        int reduction = Math.min(dailyOt, weeklyOt);
                        context.getFacts().put("dailyOtHoursMinutes", dailyOt - reduction);
                        break;
                    case PAY_HIGHER:
                        context.getFacts().put("dailyOtHoursMinutes", 0);
                        break;
                    case PRIORITIZE_WEEKLY_OT:
                        context.getFacts().put("dailyOtHoursMinutes", 0);
                        break;
                }
            }
        }
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
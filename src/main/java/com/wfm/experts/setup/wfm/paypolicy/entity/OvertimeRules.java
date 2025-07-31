//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
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
//import java.util.List;
//import java.util.Map;
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
//    private boolean enableDailyOt;
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
//        if (workedMinutes == null || workedMinutes <= 0) {
//            context.getFacts().put("finalRegularMinutes", workedMinutes);
//            return buildResult("NO_WORK_TIME", true, "No net work time to process for overtime.");
//        }
//
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//        int dailyOt = 0;
//        int weeklyOt = 0;
//        int excessHours = 0;
//
//        // Step 1: Calculate Provisional Daily OT
//        int provisionalRegularForDaily = workedMinutes;
//        if (enableDailyOt) {
//            int dailyThreshold = getDailyThresholdInMinutes(employeeShift);
//            dailyOt = Math.max(0, workedMinutes - dailyThreshold);
//            provisionalRegularForDaily = Math.min(workedMinutes, dailyThreshold);
//        }
//
//        // Step 2: Determine the pool of minutes for weekly calculation
//        int minutesPoolForWeeklyCalc = (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) ? workedMinutes : provisionalRegularForDaily;
//        if (enableWeeklyOt) {
//            weeklyOt = calculateWeeklyOvertime(context, minutesPoolForWeeklyCalc);
//        }
//
//        // Step 3: Apply Conflict Resolution
//        if (dailyOt > 0 && weeklyOt > 0 && dailyWeeklyOtConflict != null) {
//            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
//                dailyOt = 0;
//            }
//        }
//
//        // Step 4: Apply Capping
//        if (maxOtPerWeek != null && maxOtPerWeek > 0) {
//            int maxOtInMinutes = (int) (maxOtPerWeek * 60);
//            int pastOtMinutes = getPastOtMinutes(context);
//
//            int totalOtToday = dailyOt + weeklyOt;
//            int roomInOtBucket = Math.max(0, maxOtInMinutes - pastOtMinutes);
//
//            if (totalOtToday > roomInOtBucket) {
//                excessHours = totalOtToday - roomInOtBucket;
//                int otToCap = excessHours;
//
//                int weeklyOtToCap = Math.min(weeklyOt, otToCap);
//                weeklyOt -= weeklyOtToCap;
//
//                int dailyOtToCap = otToCap - weeklyOtToCap;
//                dailyOt -= dailyOtToCap;
//            }
//        }
//
//        // Step 5: Calculate Final Regular Minutes
//        int finalRegularMinutes = workedMinutes - dailyOt - weeklyOt - excessHours;
//
//        // Step 6: Set final, authoritative facts for the orchestrator
//        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
//        context.getFacts().put("weeklyOtHoursMinutes", weeklyOt);
//        context.getFacts().put("excessHoursMinutes", excessHours);
//        context.getFacts().put("finalRegularMinutes", finalRegularMinutes);
//
//        String message = String.format("OT Calculated. Daily: %d, Weekly: %d, Excess: %d. Final Regular: %d.", dailyOt, weeklyOt, excessHours, finalRegularMinutes);
//        return buildResult("OT_CALCULATED", true, message);
//    }
//
//    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) shiftDuration += 1440;
//            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
//        }
//        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
//    }
//
//    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayMinutesPool) {
//        if (!this.enableWeeklyOt || this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) {
//            return 0;
//        }
//
//        List<Timesheet> pastWeekTimesheets = getPastWeekTimesheets(context);
//
//        int pastMinutes;
//        if (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) {
//            pastMinutes = pastWeekTimesheets.stream().mapToInt(ts -> (ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0) + (ts.getExcessHoursMinutes() != null ? ts.getExcessHoursMinutes() : 0)).sum();
//        } else { // REGULAR_HOURS_ONLY
//            pastMinutes = pastWeekTimesheets.stream().mapToInt(ts -> ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0).sum();
//        }
//
//        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
//        int totalAccumulatedMinutes = pastMinutes + currentDayMinutesPool;
//
//        if (totalAccumulatedMinutes <= weeklyThresholdMinutes) {
//            return 0;
//        }
//
//        int weeklyOtForThisWeek = totalAccumulatedMinutes - weeklyThresholdMinutes;
//        int alreadyPaidWeeklyOt = getPastOtMinutes(pastWeekTimesheets);
//        int newWeeklyOt = weeklyOtForThisWeek - alreadyPaidWeeklyOt;
//
//        return Math.min(currentDayMinutesPool, Math.max(0, newWeeklyOt));
//    }
//
//    private int getPastOtMinutes(PayPolicyExecutionContext context) {
//        return getPastOtMinutes(getPastWeekTimesheets(context));
//    }
//
//    private int getPastOtMinutes(List<Timesheet> pastWeekTimesheets) {
//        return pastWeekTimesheets.stream()
//                .mapToInt(ts -> {
//                    try {
//                        if (ts.getRuleResultsJson() != null && !ts.getRuleResultsJson().isEmpty()) {
//                            ObjectMapper mapper = new ObjectMapper();
//                            List<Map<String, Object>> results = mapper.readValue(ts.getRuleResultsJson(), new TypeReference<>() {});
//                            int dailyOt = results.stream().filter(r -> "dailyOtHoursMinutes".equals(r.get("ruleName"))).mapToInt(r -> Integer.parseInt(String.valueOf(r.get("result")))).sum();
//                            int weeklyOt = results.stream().filter(r -> "WeeklyOvertime".equals(r.get("ruleName"))).mapToInt(r -> Integer.parseInt(String.valueOf(r.get("result")))).sum();
//                            return dailyOt + weeklyOt;
//                        }
//                    } catch (Exception e) {
//                        log.error("Error parsing OT from JSON for timesheet {}", ts.getId());
//                    }
//                    return 0;
//                }).sum();
//    }
//
//    private List<Timesheet> getPastWeekTimesheets(PayPolicyExecutionContext context) {
//        String employeeId = context.getEmployeeId();
//        LocalDate workDate = context.getDate();
//        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
//
//        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
//        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
//        if (workDate.isBefore(weekStartDate)) {
//            weekStartDate = weekStartDate.minusWeeks(1);
//        }
//        return timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
//    }
//
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
import java.util.List;
import java.util.Map;

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
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        if (workedMinutes == null || workedMinutes <= 0) {
            context.getFacts().put("finalRegularMinutes", 0);
            context.getFacts().put("excessHoursMinutes", 0);
            return buildResult("NO_WORK_TIME", true, "No net work time to process for overtime.");
        }

        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        int dailyOt = 0;
        int weeklyOt = 0;
        int excessHours = 0;

        // Step 1: Calculate Provisional Daily OT
        int provisionalRegularForDaily = workedMinutes;
        if (enableDailyOt) {
            int dailyThreshold = getDailyThresholdInMinutes(employeeShift);
            dailyOt = Math.max(0, workedMinutes - dailyThreshold);
            provisionalRegularForDaily = Math.min(workedMinutes, dailyThreshold);
        }

        // Step 2: Determine the pool of minutes for weekly calculation
        int minutesPoolForWeeklyCalc = (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) ? workedMinutes : provisionalRegularForDaily;
        if (enableWeeklyOt) {
            weeklyOt = calculateWeeklyOvertime(context, minutesPoolForWeeklyCalc);
        }

        // Step 3: Apply Conflict Resolution
        if (dailyOt > 0 && weeklyOt > 0 && dailyWeeklyOtConflict != null) {
            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
                dailyOt = 0;
            }
        }

        // Step 4: Apply Capping
        if (maxOtPerWeek != null && maxOtPerWeek > 0) {
            int maxOtInMinutes = (int) (maxOtPerWeek * 60);
            int pastOtMinutes = getPastOtMinutes(context);

            int totalOtToday = dailyOt + weeklyOt;
            int roomInOtBucket = Math.max(0, maxOtInMinutes - pastOtMinutes);

            if (totalOtToday > roomInOtBucket) {
                excessHours = totalOtToday - roomInOtBucket;
                int otToCap = excessHours;

                int weeklyOtToCap = Math.min(weeklyOt, otToCap);
                weeklyOt -= weeklyOtToCap;

                int dailyOtToCap = otToCap - weeklyOtToCap;
                dailyOt -= dailyOtToCap;
            }
        }

        // Step 5: Calculate Final Regular Minutes
        int finalRegularMinutes = workedMinutes - dailyOt - weeklyOt - excessHours;

        // Step 6: Set final, authoritative facts for the orchestrator
        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOt);
        context.getFacts().put("excessHoursMinutes", excessHours);
        context.getFacts().put("finalRegularMinutes", finalRegularMinutes);

        String message = String.format("OT Calculated. Daily: %d, Weekly: %d, Excess: %d. Final Regular: %d.", dailyOt, weeklyOt, excessHours, finalRegularMinutes);
        return buildResult("OT_CALCULATED", true, message);
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) shiftDuration += 1440;
            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
        }
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
    }

    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayMinutesPool) {
        if (!this.enableWeeklyOt || this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) return 0;

        List<Timesheet> pastWeekTimesheets = getPastWeekTimesheets(context);

        int pastMinutes;
        if (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) {
            pastMinutes = pastWeekTimesheets.stream().mapToInt(ts -> (ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0) + (ts.getExcessHoursMinutes() != null ? ts.getExcessHoursMinutes() : 0)).sum();
        } else { // REGULAR_HOURS_ONLY
            pastMinutes = pastWeekTimesheets.stream().mapToInt(ts -> ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0).sum();
        }

        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
        int totalAccumulatedMinutes = pastMinutes + currentDayMinutesPool;

        if (totalAccumulatedMinutes <= weeklyThresholdMinutes) return 0;

        int weeklyOtForThisWeek = totalAccumulatedMinutes - weeklyThresholdMinutes;
        int alreadyPaidWeeklyOt = getPastOtMinutes(pastWeekTimesheets); // Corrected Call
        int newWeeklyOt = weeklyOtForThisWeek - alreadyPaidWeeklyOt;

        return Math.min(currentDayMinutesPool, Math.max(0, newWeeklyOt));
    }

    private int getPastOtMinutes(PayPolicyExecutionContext context) {
        return getPastOtMinutes(getPastWeekTimesheets(context));
    }

    private int getPastOtMinutes(List<Timesheet> pastWeekTimesheets) {
        return pastWeekTimesheets.stream().mapToInt(ts -> {
            try {
                if (ts.getRuleResultsJson() != null && !ts.getRuleResultsJson().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> results = mapper.readValue(ts.getRuleResultsJson(), new TypeReference<>() {});
                    int dailyOt = results.stream().filter(r -> "dailyOtHoursMinutes".equals(r.get("ruleName"))).mapToInt(r -> Integer.parseInt(String.valueOf(r.get("result")))).sum();
                    int weeklyOt = results.stream().filter(r -> "WeeklyOvertime".equals(r.get("ruleName"))).mapToInt(r -> Integer.parseInt(String.valueOf(r.get("result")))).sum();
                    return dailyOt + weeklyOt;
                }
            } catch (Exception e) {
                log.error("Error parsing OT from JSON for timesheet {}", ts.getId());
            }
            return 0;
        }).sum();
    }

    private List<Timesheet> getPastWeekTimesheets(PayPolicyExecutionContext context) {
        String employeeId = context.getEmployeeId();
        LocalDate workDate = context.getDate();
        TimesheetRepository timesheetRepository = context.getTimesheetRepository();

        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
        if (workDate.isBefore(weekStartDate)) {
            weekStartDate = weekStartDate.minusWeeks(1);
        }
        return timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
    }

    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
        return PayPolicyRuleResultDTO.builder().ruleName(getName()).result(result).success(success).message(message).build();
    }
}
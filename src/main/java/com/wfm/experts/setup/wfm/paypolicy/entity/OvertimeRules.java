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
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer grossWorkedMinutes = (Integer) context.getFact("grossWorkMinutes");

        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
            context.getFacts().put("dailyOtHoursMinutes", 0);
            context.getFacts().put("weeklyOtHoursMinutes", 0);
            context.getFacts().put("excessHoursMinutes", 0);
            context.getFacts().put("finalRegularMinutes", 0);
            return buildResult("NO_WORK_TIME_FOR_OT", true, "No work time to process for overtime.", "{}");
        }

        boolean isHoliday = (boolean) context.getFacts().getOrDefault("isHoliday", false);
        boolean isWeekend = (boolean) context.getFacts().getOrDefault("isWeekend", false);
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        long shiftDuration = -1;
        if (employeeShift != null && employeeShift.getShift() != null) {
            shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) shiftDuration += 1440; // Handles overnight shifts
        }

        // *** FIX: Logic block for Holidays and Weekends ***
        if (isHoliday || isWeekend) {
            Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
            int totalPaidTime = grossWorkedMinutes - unpaidBreakMinutes;
            int specialDayWorkedMinutes = (int) (isHoliday ? context.getFacts().getOrDefault("holidayWorkedMinutes", 0) : context.getFacts().getOrDefault("weekendWorkedMinutes", 0));

            // Excess is the total paid time MINUS the time already categorized as holiday/weekend work.
            int excessOnSpecialDay = Math.max(0, totalPaidTime - specialDayWorkedMinutes);

            context.getFacts().put("dailyOtHoursMinutes", 0);
            context.getFacts().put("weeklyOtHoursMinutes", 0);
            context.getFacts().put("excessHoursMinutes", excessOnSpecialDay);
            context.getFacts().put("finalRegularMinutes", 0); // No regular minutes on these days

            Map<String, Integer> resultPayload = new HashMap<>();
            resultPayload.put("dailyOt", 0);
            resultPayload.put("weeklyOt", 0);
            resultPayload.put("excessHours", excessOnSpecialDay);
            resultPayload.put("finalRegularMinutes", 0);

            String message = String.format("Special Day Calculation. Excess: %d.", excessOnSpecialDay);
            try {
                return buildResult("EXCESS_CALCULATED", true, message, new ObjectMapper().writeValueAsString(resultPayload));
            } catch (JsonProcessingException e) {
                return buildResult("EXCESS_CALCULATED", true, message, "{}");
            }
        }

        // --- Logic for Regular Workdays ---
        Integer netWorkedMinutes = (Integer) context.getFact("workedMinutes");
        int minutesEligibleForCalc = netWorkedMinutes != null ? netWorkedMinutes : 0;

        int dailyOt = 0;
        int weeklyOt = 0;
        int excessHours = 0;
        int regularMinutesForToday = 0;

        if (enableDailyOt) {
            if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && shiftDuration > 0) {
                long minutesPastShiftEnd = minutesEligibleForCalc - shiftDuration;
                int grace = (gracePeriodAfterShiftEnd != null ? gracePeriodAfterShiftEnd : 0);
                if (minutesPastShiftEnd > grace) {
                    dailyOt = (int) minutesPastShiftEnd;
                }
                regularMinutesForToday = (int) Math.min(minutesEligibleForCalc, shiftDuration);
            } else if (dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
                int dailyThreshold = getDailyThresholdInMinutes();
                dailyOt = Math.max(0, minutesEligibleForCalc - dailyThreshold);
                regularMinutesForToday = Math.min(minutesEligibleForCalc, dailyThreshold);
            } else {
                regularMinutesForToday = minutesEligibleForCalc;
            }
        } else {
            if(shiftDuration > 0) {
                regularMinutesForToday = (int) Math.min(minutesEligibleForCalc, shiftDuration);
                excessHours = Math.max(0, minutesEligibleForCalc - (int) shiftDuration);
            } else {
                regularMinutesForToday = minutesEligibleForCalc;
            }
        }

        if (enableDailyOt && maxOtPerDay != null && maxOtPerDay > 0) {
            int maxDailyOtInMinutes = (int) (maxOtPerDay * 60);
            if (dailyOt > maxDailyOtInMinutes) {
                excessHours += dailyOt - maxDailyOtInMinutes;
                dailyOt = maxDailyOtInMinutes;
            }
        }

        if (enableWeeklyOt && weeklyThresholdHours != null && weeklyThresholdHours > 0) {
            List<Timesheet> pastWeekTimesheets = getPastWeekTimesheets(context);
            int weeklyThresholdMinutes = weeklyThresholdHours * 60;
            int pastRegularForWeekly = getPastRegularMinutesForWeeklyOt(pastWeekTimesheets);
            int minutesNeededForThreshold = Math.max(0, weeklyThresholdMinutes - pastRegularForWeekly);
            int contributionToRegular = Math.min(regularMinutesForToday, minutesNeededForThreshold);
            weeklyOt = regularMinutesForToday - contributionToRegular;
            regularMinutesForToday = contributionToRegular;
        }

        if (dailyOt > 0 && weeklyOt > 0 && dailyWeeklyOtConflict != null) {
            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
                regularMinutesForToday += dailyOt;
                weeklyOt += dailyOt;
                dailyOt = 0;
            }
        }

        if (enableWeeklyOt && maxOtPerWeek != null && maxOtPerWeek > 0) {
            int maxWeeklyOtInMinutes = (int) (maxOtPerWeek * 60);
            List<Timesheet> pastWeekTimesheets = getPastWeekTimesheets(context);
            int pastWeeklyOt = getPastOtMinutes(pastWeekTimesheets);
            int roomInWeeklyBucket = Math.max(0, maxWeeklyOtInMinutes - pastWeeklyOt);
            if (weeklyOt > roomInWeeklyBucket) {
                excessHours += weeklyOt - roomInWeeklyBucket;
                weeklyOt = roomInWeeklyBucket;
            }
        }

        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOt);
        context.getFacts().put("excessHoursMinutes", excessHours);
        context.getFacts().put("finalRegularMinutes", regularMinutesForToday);

        Map<String, Integer> resultPayload = new HashMap<>();
        resultPayload.put("dailyOt", dailyOt);
        resultPayload.put("weeklyOt", weeklyOt);
        resultPayload.put("excessHours", excessHours);
        resultPayload.put("finalRegularMinutes", regularMinutesForToday);

        String resultJson;
        try {
            resultJson = new ObjectMapper().writeValueAsString(resultPayload);
        } catch (JsonProcessingException e) {
            log.error("Error serializing OT results", e);
            resultJson = "{}";
        }

        String message = String.format("OT Calculated. Daily: %d, Weekly: %d, Excess: %d. Final Regular: %d.", dailyOt, weeklyOt, excessHours, regularMinutesForToday);
        return buildResult("OT_CALCULATED", true, message, resultJson);
    }

    private int getDailyThresholdInMinutes() {
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
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

    private int getPastRegularMinutesForWeeklyOt(List<Timesheet> pastWeekTimesheets) {
        return pastWeekTimesheets.stream().mapToInt(ts -> {
            try {
                if (ts.getRuleResultsJson() != null && !ts.getRuleResultsJson().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<PayPolicyRuleResultDTO> results = mapper.readValue(ts.getRuleResultsJson(), new TypeReference<>() {});

                    boolean wasHoliday = results.stream().anyMatch(r -> "HolidayPayRules".equals(r.getRuleName()));
                    boolean wasWeekend = results.stream().anyMatch(r -> "WeekendPayRules".equals(r.getRuleName()));

                    if (wasHoliday || wasWeekend) {
                        return 0;
                    }

                    return results.stream()
                            .filter(r -> "OvertimeRules".equals(r.getRuleName()))
                            .findFirst()
                            .map(otRuleResult -> {
                                try {
                                    Map<String, Integer> otMap = mapper.readValue(otRuleResult.getResult(), new TypeReference<>() {});
                                    if (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) {
                                        return otMap.getOrDefault("finalRegularMinutes", 0) + otMap.getOrDefault("dailyOt", 0);
                                    }
                                    return otMap.getOrDefault("finalRegularMinutes", 0);
                                } catch (Exception e) {
                                    log.error("Error parsing OT Map from JSON for timesheet {}. Returning 0.", ts.getId(), e);
                                    return 0;
                                }
                            }).orElse(ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0);
                }
            } catch (Exception e) {
                log.error("Error parsing RuleResultsJSON for timesheet {}. Falling back. Error: {}", ts.getId(), e.getMessage());
            }
            return ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0;
        }).sum();
    }

    private int getPastOtMinutes(List<Timesheet> pastWeekTimesheets) {
        return pastWeekTimesheets.stream().mapToInt(ts -> {
            try {
                if (ts.getRuleResultsJson() != null && !ts.getRuleResultsJson().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<PayPolicyRuleResultDTO> results = mapper.readValue(ts.getRuleResultsJson(), new TypeReference<>() {});
                    Optional<PayPolicyRuleResultDTO> otRuleResult = results.stream()
                            .filter(r -> "OvertimeRules".equals(r.getRuleName()))
                            .findFirst();
                    if (otRuleResult.isPresent()) {
                        Map<String, Integer> otMap = mapper.readValue(otRuleResult.get().getResult(), new TypeReference<>() {});
                        return otMap.getOrDefault("weeklyOt", 0);
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing OT from JSON for timesheet {}", ts.getId(), e);
            }
            return 0;
        }).sum();
    }

    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message, String resultJson) {
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(resultJson)
                .success(success)
                .message(message)
                .build();
    }
}
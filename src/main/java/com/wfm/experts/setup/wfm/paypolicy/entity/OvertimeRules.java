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
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        if (workedMinutes == null || workedMinutes <= 0) {
            context.getFacts().put("finalRegularMinutes", 0);
            context.getFacts().put("excessHoursMinutes", 0);
            return buildResult("NO_WORK_TIME", true, "No net work time to process for overtime.", "{}");
        }

        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        int dailyOt = 0;
        int weeklyOt = 0;
        int excessHours = 0;
        int finalRegularMinutes = 0;

        List<Timesheet> pastWeekTimesheets = getPastWeekTimesheets(context);

        // Step 1: Calculate Provisional Daily OT
        if (enableDailyOt) {
            if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
                long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
                if (shiftDuration < 0) shiftDuration += 1440;

                long minutesPastShiftEnd = workedMinutes - shiftDuration;
                int grace = (gracePeriodAfterShiftEnd != null ? gracePeriodAfterShiftEnd : 0);

                if (minutesPastShiftEnd > grace) {
                    dailyOt = (int) minutesPastShiftEnd;
                } else {
                    dailyOt = 0;
                }
            } else if (dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
                int dailyThreshold = getDailyThresholdInMinutes();
                dailyOt = Math.max(0, workedMinutes - dailyThreshold);
            }
        }

        // Apply Daily OT Capping and Calculate Excess Hours
        if (enableDailyOt && maxOtPerDay != null && maxOtPerDay > 0) {
            int maxDailyOtInMinutes = (int) (maxOtPerDay * 60);
            if (dailyOt > maxDailyOtInMinutes) {
                excessHours += dailyOt - maxDailyOtInMinutes;
                dailyOt = maxDailyOtInMinutes;
            }
        }

        // Step 2: Calculate Weekly OT based on remaining regular minutes
        int minutesAvailableForWeeklyProcessing = workedMinutes - dailyOt - excessHours;

        if (enableWeeklyOt && weeklyThresholdHours != null && weeklyThresholdHours > 0) {
            int weeklyThresholdMinutes = weeklyThresholdHours * 60;
            int pastRegularMinutesForWeekly = getPastRegularMinutesForWeeklyOt(pastWeekTimesheets);

            if (pastRegularMinutesForWeekly < weeklyThresholdMinutes) {
                int minutesUntilThreshold = weeklyThresholdMinutes - pastRegularMinutesForWeekly;
                finalRegularMinutes = Math.min(minutesAvailableForWeeklyProcessing, minutesUntilThreshold);
                weeklyOt = Math.max(0, minutesAvailableForWeeklyProcessing - finalRegularMinutes);
            } else {
                finalRegularMinutes = 0;
                weeklyOt = minutesAvailableForWeeklyProcessing;
            }
        } else {
            finalRegularMinutes = minutesAvailableForWeeklyProcessing;
        }


        // Step 3: Apply Conflict Resolution
        if (dailyOt > 0 && weeklyOt > 0 && dailyWeeklyOtConflict != null) {
            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
                dailyOt = 0;
            }
        }

        // Step 4: Apply Weekly Capping
        if (maxOtPerWeek != null && maxOtPerWeek > 0) {
            int maxOtInMinutes = (int) (maxOtPerWeek * 60);
            int pastOtMinutes = getPastOtMinutes(pastWeekTimesheets); // Use already fetched timesheets
            int totalOtForToday = dailyOt + weeklyOt;
            int roomInOtBucket = Math.max(0, maxOtInMinutes - pastOtMinutes);

            if (totalOtForToday > roomInOtBucket) {
                int overflow = totalOtForToday - roomInOtBucket;
                excessHours += overflow;

                int weeklyOtToReduce = Math.min(weeklyOt, overflow);
                weeklyOt -= weeklyOtToReduce;

                int dailyOtToReduce = overflow - weeklyOtToReduce;
                dailyOt -= dailyOtToReduce;
            }
        }


        // Step 5: Final Sanity Check on Regular Minutes & Excess Hour Calculation
        finalRegularMinutes = workedMinutes - dailyOt - weeklyOt - excessHours;

        if (employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) shiftDuration += 1440; // Handles overnight shifts

            if (finalRegularMinutes > shiftDuration) {
                excessHours += finalRegularMinutes - shiftDuration;
                finalRegularMinutes = (int) shiftDuration;
            }
        }


        // Step 6: Set final, authoritative facts for the orchestrator
        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("weeklyOtHoursMinutes", weeklyOt);
        context.getFacts().put("excessHoursMinutes", excessHours);
        context.getFacts().put("finalRegularMinutes", finalRegularMinutes);

        // Create a detailed result payload
        Map<String, Integer> resultPayload = new HashMap<>();
        resultPayload.put("dailyOt", dailyOt);
        resultPayload.put("weeklyOt", weeklyOt);
        resultPayload.put("excessHours", excessHours);
        resultPayload.put("finalRegularMinutes", finalRegularMinutes);

        String resultJson;
        try {
            resultJson = new ObjectMapper().writeValueAsString(resultPayload);
        } catch (JsonProcessingException e) {
            log.error("Error serializing OT results", e);
            resultJson = "{}";
        }


        String message = String.format("OT Calculated. Daily: %d, Weekly: %d, Excess: %d. Final Regular: %d.", dailyOt, weeklyOt, excessHours, finalRegularMinutes);
        return buildResult("OT_CALCULATED", true, message, resultJson);
    }

    private int getDailyThresholdInMinutes() {
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
    }

    private int getPastRegularMinutesForWeeklyOt(List<Timesheet> pastWeekTimesheets) {
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
                        if (weeklyOtBasis == WeeklyOtBasis.REGULAR_AND_DAILY_OT) {
                            return otMap.getOrDefault("finalRegularMinutes", 0) + otMap.getOrDefault("dailyOt", 0);
                        } else { // REGULAR_HOURS_ONLY
                            return otMap.getOrDefault("finalRegularMinutes", 0);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing Regular Minutes from JSON for timesheet {}", ts.getId(), e);
            }
            // Fallback if JSON parsing fails
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
                        int dailyOt = otMap.getOrDefault("dailyOt", 0);
                        int weeklyOt = otMap.getOrDefault("weeklyOt", 0);
                        return dailyOt + weeklyOt;
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing OT from JSON for timesheet {}", ts.getId(), e);
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

    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message, String resultJson) {
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(resultJson)
                .success(success)
                .message(message)
                .build();
    }
}
////package com.wfm.experts.setup.wfm.paypolicy.entity;
////
////import com.fasterxml.jackson.core.type.TypeReference;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
////import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
////import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
////import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
////import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
////import com.wfm.experts.setup.wfm.paypolicy.enums.*;
////import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
////import com.wfm.experts.setup.wfm.shift.entity.Shift;
////import jakarta.persistence.*;
////import lombok.*;
////import lombok.extern.slf4j.Slf4j;
////
////import java.time.DayOfWeek;
////import java.time.Duration;
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.util.List;
////import java.util.Optional;
////
////@Slf4j
////@Entity
////@Table(name = "overtime_rules")
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////@Builder
////public class OvertimeRules implements PayPolicyRule {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    private boolean enabled;
////    private boolean enableDailyOt;
////    private Integer thresholdHours;
////    private Integer thresholdMinutes;
////    private Double maxOtPerDay;
////    private Double maxOtPerWeek;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private OvertimeConflictResolution conflictResolution;
////
////    private boolean resetOtBucketDaily;
////    private boolean resetOtBucketWeekly;
////    private boolean resetOtBucketOnPayPeriod;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private CompensationMethod compensationMethod;
////
////    private Double paidOtMultiplier;
////    private Integer compOffDaysPerOt;
////    private Integer compOffHoursPerOt;
////    private Integer maxCompOffBalance;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private CompOffBalanceBasis maxCompOffBalanceBasis;
////
////    private Integer compOffExpiryValue;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 10)
////    private ExpiryUnit compOffExpiryUnit;
////
////    private boolean encashOnExpiry;
////
////    @OneToOne(cascade = CascadeType.ALL)
////    @JoinColumn(name = "pre_shift_inclusion_id")
////    private PreShiftInclusion preShiftInclusion;
////
////    @ManyToMany(fetch = FetchType.EAGER)
////    @JoinTable(
////            name = "overtime_rules_shifts",
////            joinColumns = @JoinColumn(name = "overtime_rules_id"),
////            inverseJoinColumns = @JoinColumn(name = "shift_id")
////    )
////    private List<Shift> shifts;
////
////    @Enumerated(EnumType.STRING)
////    private DailyOtTrigger dailyOtTrigger;
////
////    private Integer gracePeriodAfterShiftEnd;
////
////    private boolean enableWeeklyOt;
////
////    private Integer weeklyThresholdHours;
////
////    @Enumerated(EnumType.STRING)
////    private WeeklyOtBasis weeklyOtBasis;
////
////    @Enumerated(EnumType.STRING)
////    private DailyWeeklyOtConflict dailyWeeklyOtConflict;
////
////    @Enumerated(EnumType.STRING)
////    private WeekDay weeklyResetDay;
////
////    @Override
////    public String getName() {
////        return "OvertimeRules";
////    }
////
////    @Override
////    public boolean evaluate(PayPolicyExecutionContext context) {
////        return enabled && context.getFact("workedMinutes") != null;
////    }
////
////    @Override
////    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
////        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
////        if (workedMinutes == null || workedMinutes <= 0) {
////            return buildResult("NO_WORK_TIME", true, "No gross work time to process for overtime.");
////        }
////
////        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
////
////        int dailyOtMinutes = 0;
////        if (this.enableDailyOt) {
////            dailyOtMinutes = calculateDailyOvertime(workedMinutes, employeeShift);
////        }
////
////        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
////            int maxDailyOt = (int) (this.maxOtPerDay * 60);
////            dailyOtMinutes = Math.min(dailyOtMinutes, maxDailyOt);
////        }
////        context.getFacts().put("dailyOtHoursMinutes", dailyOtMinutes);
////
////        int weeklyOtMinutes = 0;
////        if (enableWeeklyOt) {
////            int regularHoursForToday = workedMinutes - dailyOtMinutes;
////            weeklyOtMinutes = calculateWeeklyOvertime(context, regularHoursForToday);
////        }
////        context.getFacts().put("weeklyOtHoursMinutes", weeklyOtMinutes);
////
////        String message = "Identified Daily OT: " + dailyOtMinutes + ", Identified Weekly OT: " + weeklyOtMinutes;
////        return buildResult("OT_IDENTIFIED", true, message);
////    }
////
////    private int calculateDailyOvertime(Integer grossWorkedMinutes, EmployeeShift employeeShift) {
////        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
////            return 0;
////        }
////        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift);
////        return Math.max(0, grossWorkedMinutes - thresholdInMinutes);
////    }
////
////    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
////        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
////            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
////            if (shiftDuration < 0) {
////                shiftDuration += 1440;
////            }
////            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
////        }
////        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
////    }
////
////    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayRegularMinutes) {
////        if (this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) {
////            return 0;
////        }
////
////        String employeeId = context.getEmployeeId();
////        LocalDate workDate = context.getDate();
////        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
////
////        // --- 1. Determine Week Start and Get Past Data ---
////        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
////        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
////        if (workDate.isBefore(weekStartDate)) {
////            weekStartDate = weekStartDate.minusWeeks(1);
////        }
////        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
////
////        int pastRegularMinutes = pastWeekTimesheets.stream()
////                .mapToInt(sheet -> sheet.getRegularHoursMinutes() != null ? sheet.getRegularHoursMinutes() : 0)
////                .sum();
////        int pastWeeklyOtMinutes = getPastWeeklyOtFromJson(pastWeekTimesheets);
////
////        log.debug("Weekly OT Calc for {}: pastRegular={}, pastWeeklyOt={}", workDate, pastRegularMinutes, pastWeeklyOtMinutes);
////
////        // --- 2. Determine Potential OT for Today ---
////        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
////        int totalRegularConsidered = pastRegularMinutes + currentDayRegularMinutes;
////
////        if (totalRegularConsidered <= weeklyThresholdMinutes) {
////            log.debug("Total regular hours ({}) does not exceed weekly threshold ({}). No weekly OT.", totalRegularConsidered, weeklyThresholdMinutes);
////            return 0;
////        }
////
////        int potentialOtForToday;
////        if (pastRegularMinutes >= weeklyThresholdMinutes) {
////            // The threshold was already met on a previous day. All of today's regular time is potential OT.
////            potentialOtForToday = currentDayRegularMinutes;
////        } else {
////            // The threshold is crossed today. Only the amount over the threshold is potential OT.
////            potentialOtForToday = totalRegularConsidered - weeklyThresholdMinutes;
////        }
////        log.debug("Potential OT for today (uncapped): {}", potentialOtForToday);
////
////        // --- 3. Apply Weekly OT Cap ---
////        if (this.maxOtPerWeek != null && this.maxOtPerWeek > 0) {
////            int maxWeeklyOtInMinutes = (int) (this.maxOtPerWeek * 60);
////            int remainingOtCapacity = maxWeeklyOtInMinutes - pastWeeklyOtMinutes;
////            log.debug("Max weekly OT: {}, Remaining capacity: {}", maxWeeklyOtInMinutes, remainingOtCapacity);
////
////            if (remainingOtCapacity <= 0) {
////                log.debug("Weekly OT cap already met or exceeded. No more weekly OT will be allocated.");
////                return 0; // OT bucket is full
////            }
////
////            potentialOtForToday = Math.min(potentialOtForToday, remainingOtCapacity);
////        }
////
////        log.debug("Final weekly OT for today (capped): {}", potentialOtForToday);
////        return Math.max(0, potentialOtForToday);
////    }
////
////    private int getPastWeeklyOtFromJson(List<Timesheet> pastWeekTimesheets) {
////        ObjectMapper mapper = new ObjectMapper();
////        int pastWeeklyOt = 0;
////        for (Timesheet sheet : pastWeekTimesheets) {
////            if (sheet.getRuleResultsJson() != null && !sheet.getRuleResultsJson().isEmpty()) {
////                try {
////                    List<PayPolicyRuleResultDTO> results = mapper.readValue(sheet.getRuleResultsJson(), new TypeReference<List<PayPolicyRuleResultDTO>>() {});
////                    Optional<PayPolicyRuleResultDTO> otResult = results.stream().filter(r -> "OvertimeRules".equals(r.getRuleName())).findFirst();
////                    if (otResult.isPresent()) {
////                        String msg = otResult.get().getMessage();
////                        if (msg.contains("Identified Weekly OT:")) {
////                            String weeklyOtStr = msg.substring(msg.indexOf("Identified Weekly OT:") + "Identified Weekly OT:".length()).trim();
////                            weeklyOtStr = weeklyOtStr.split(" ")[0];
////                            pastWeeklyOt += Integer.parseInt(weeklyOtStr);
////                        }
////                    }
////                } catch (Exception e) {
////                    log.error("Could not parse rule results JSON for timesheet " + sheet.getId(), e);
////                }
////            }
////        }
////        return pastWeeklyOt;
////    }
////
////    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
////        return PayPolicyRuleResultDTO.builder()
////                .ruleName(getName())
////                .result(result)
////                .success(success)
////                .message(message)
////                .build();
////    }
////}
////package com.wfm.experts.setup.wfm.paypolicy.entity;
////
////import com.fasterxml.jackson.core.type.TypeReference;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
////import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
////import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
////import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
////import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
////import com.wfm.experts.setup.wfm.paypolicy.enums.*;
////import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
////import com.wfm.experts.setup.wfm.shift.entity.Shift;
////import jakarta.persistence.*;
////import lombok.*;
////import lombok.extern.slf4j.Slf4j;
////
////import java.time.DayOfWeek;
////import java.time.Duration;
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.util.List;
////import java.util.Optional;
////
////@Slf4j
////@Entity
////@Table(name = "overtime_rules")
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////@Builder
////public class OvertimeRules implements PayPolicyRule {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    private boolean enabled;
////    private boolean enableDailyOt;
////    private Integer thresholdHours;
////    private Integer thresholdMinutes;
////    private Double maxOtPerDay;
////    private Double maxOtPerWeek;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private OvertimeConflictResolution conflictResolution;
////
////    private boolean resetOtBucketDaily;
////    private boolean resetOtBucketWeekly;
////    private boolean resetOtBucketOnPayPeriod;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private CompensationMethod compensationMethod;
////
////    private Double paidOtMultiplier;
////    private Integer compOffDaysPerOt;
////    private Integer compOffHoursPerOt;
////    private Integer maxCompOffBalance;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private CompOffBalanceBasis maxCompOffBalanceBasis;
////
////    private Integer compOffExpiryValue;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 10)
////    private ExpiryUnit compOffExpiryUnit;
////
////    private boolean encashOnExpiry;
////
////    @OneToOne(cascade = CascadeType.ALL)
////    @JoinColumn(name = "pre_shift_inclusion_id")
////    private PreShiftInclusion preShiftInclusion;
////
////    @ManyToMany(fetch = FetchType.EAGER)
////    @JoinTable(
////            name = "overtime_rules_shifts",
////            joinColumns = @JoinColumn(name = "overtime_rules_id"),
////            inverseJoinColumns = @JoinColumn(name = "shift_id")
////    )
////    private List<Shift> shifts;
////
////    @Enumerated(EnumType.STRING)
////    private DailyOtTrigger dailyOtTrigger;
////
////    private Integer gracePeriodAfterShiftEnd;
////
////    private boolean enableWeeklyOt;
////
////    private Integer weeklyThresholdHours;
////
////    @Enumerated(EnumType.STRING)
////    private WeeklyOtBasis weeklyOtBasis;
////
////    @Enumerated(EnumType.STRING)
////    private DailyWeeklyOtConflict dailyWeeklyOtConflict;
////
////    @Enumerated(EnumType.STRING)
////    private WeekDay weeklyResetDay;
////
////    @Override
////    public String getName() {
////        return "OvertimeRules";
////    }
////
////    @Override
////    public boolean evaluate(PayPolicyExecutionContext context) {
////        return enabled && context.getFact("workedMinutes") != null;
////    }
////
////    @Override
////    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
////        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
////        if (workedMinutes == null || workedMinutes <= 0) {
////            return buildResult("NO_WORK_TIME", true, "No gross work time to process for overtime.");
////        }
////
////        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
////
////        int potentialDailyOt = 0;
////        if (this.enableDailyOt) {
////            potentialDailyOt = calculateDailyOvertime(workedMinutes, employeeShift);
////        }
////
////        int regularHoursForWeeklyCalc = workedMinutes - potentialDailyOt;
////        int potentialWeeklyOt = 0;
////        if (enableWeeklyOt) {
////            potentialWeeklyOt = calculateWeeklyOvertime(context, regularHoursForWeeklyCalc);
////        }
////
////        // --- CONFLICT RESOLUTION LOGIC ---
////        int finalDailyOt = potentialDailyOt;
////        int finalWeeklyOt = potentialWeeklyOt;
////
////        if (potentialDailyOt > 0 && potentialWeeklyOt > 0 && dailyWeeklyOtConflict != null) {
////            switch (dailyWeeklyOtConflict) {
////                case EXCLUDE_DAILY_OT: // This matches your description: "if weekly ot triggered you do not trigger daily"
////                    log.debug("Conflict Resolution: EXCLUDE_DAILY_OT. Weekly OT ({}) > 0, so Daily OT is set to 0.", potentialWeeklyOt);
////                    finalDailyOt = 0;
////                    break;
////                case PRIORITIZE_WEEKLY_OT: // Similar to above, weekly takes precedence
////                    log.debug("Conflict Resolution: PRIORITIZE_WEEKLY_OT. Weekly OT ({}) > 0, so Daily OT is set to 0.", potentialWeeklyOt);
////                    finalDailyOt = 0;
////                    break;
////                case PAY_BOTH:
////                    log.debug("Conflict Resolution: PAY_BOTH. Both Daily ({}) and Weekly ({}) OT are kept.", potentialDailyOt, potentialWeeklyOt);
////                    // No change needed, both are kept
////                    break;
////                // NOTE: PAY_HIGHER would require rate information, which is not available here.
////                // It's often handled in the payroll calculation step, not the hour classification step.
////                // For now, it will behave like PAY_BOTH.
////                default:
////                    // Default behavior if not specified or another enum is added
////                    break;
////            }
////        }
////
////        // Apply caps after conflict resolution
////        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
////            finalDailyOt = Math.min(finalDailyOt, (int) (this.maxOtPerDay * 60));
////        }
////
////        // The weekly cap is already handled inside calculateWeeklyOvertime
////        context.getFacts().put("dailyOtHoursMinutes", finalDailyOt);
////        context.getFacts().put("weeklyOtHoursMinutes", finalWeeklyOt);
////
////        String message = "Identified Daily OT: " + finalDailyOt + ", Identified Weekly OT: " + finalWeeklyOt;
////        return buildResult("OT_IDENTIFIED", true, message);
////    }
////
////    private int calculateDailyOvertime(Integer grossWorkedMinutes, EmployeeShift employeeShift) {
////        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
////            return 0;
////        }
////        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift);
////        return Math.max(0, grossWorkedMinutes - thresholdInMinutes);
////    }
////
////    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
////        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
////            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
////            if (shiftDuration < 0) {
////                shiftDuration += 1440;
////            }
////            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
////        }
////        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
////    }
////
////    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayRegularMinutes) {
////        if (!this.enableWeeklyOt || this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) {
////            return 0;
////        }
////
////        String employeeId = context.getEmployeeId();
////        LocalDate workDate = context.getDate();
////        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
////
////        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
////        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
////        if (workDate.isBefore(weekStartDate)) {
////            weekStartDate = weekStartDate.minusWeeks(1);
////        }
////        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
////
////        int pastRegularMinutes = pastWeekTimesheets.stream()
////                .mapToInt(sheet -> sheet.getRegularHoursMinutes() != null ? sheet.getRegularHoursMinutes() : 0)
////                .sum();
////        int pastWeeklyOtMinutes = getPastWeeklyOtFromJson(pastWeekTimesheets);
////
////        log.debug("Weekly OT Calc for {}: pastRegular={}, pastWeeklyOt={}", workDate, pastRegularMinutes, pastWeeklyOtMinutes);
////
////        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
////        int totalRegularConsidered = pastRegularMinutes + currentDayRegularMinutes;
////
////        if (totalRegularConsidered <= weeklyThresholdMinutes) {
////            log.debug("Total regular hours ({}) does not exceed weekly threshold ({}). No weekly OT.", totalRegularConsidered, weeklyThresholdMinutes);
////            return 0;
////        }
////
////        int potentialOtForToday;
////        if (pastRegularMinutes >= weeklyThresholdMinutes) {
////            potentialOtForToday = currentDayRegularMinutes;
////        } else {
////            potentialOtForToday = totalRegularConsidered - weeklyThresholdMinutes;
////        }
////        log.debug("Potential OT for today (uncapped): {}", potentialOtForToday);
////
////        if (this.maxOtPerWeek != null && this.maxOtPerWeek > 0) {
////            int maxWeeklyOtInMinutes = (int) (this.maxOtPerWeek * 60);
////            int remainingOtCapacity = maxWeeklyOtInMinutes - pastWeeklyOtMinutes;
////            log.debug("Max weekly OT: {}, Remaining capacity: {}", maxWeeklyOtInMinutes, remainingOtCapacity);
////
////            if (remainingOtCapacity <= 0) {
////                log.debug("Weekly OT cap already met or exceeded. No more weekly OT will be allocated.");
////                return 0;
////            }
////
////            potentialOtForToday = Math.min(potentialOtForToday, remainingOtCapacity);
////        }
////
////        log.debug("Final weekly OT for today (capped): {}", potentialOtForToday);
////        return Math.max(0, potentialOtForToday);
////    }
////
////    private int getPastWeeklyOtFromJson(List<Timesheet> pastWeekTimesheets) {
////        ObjectMapper mapper = new ObjectMapper();
////        int pastWeeklyOt = 0;
////        for (Timesheet sheet : pastWeekTimesheets) {
////            if (sheet.getRuleResultsJson() != null && !sheet.getRuleResultsJson().isEmpty()) {
////                try {
////                    List<PayPolicyRuleResultDTO> results = mapper.readValue(sheet.getRuleResultsJson(), new TypeReference<List<PayPolicyRuleResultDTO>>() {});
////                    Optional<PayPolicyRuleResultDTO> otResult = results.stream().filter(r -> "OvertimeRules".equals(r.getRuleName())).findFirst();
////                    if (otResult.isPresent()) {
////                        String msg = otResult.get().getMessage();
////                        if (msg.contains("Identified Weekly OT:")) {
////                            String weeklyOtStr = msg.substring(msg.indexOf("Identified Weekly OT:") + "Identified Weekly OT:".length()).trim();
////                            weeklyOtStr = weeklyOtStr.split(" ")[0];
////                            pastWeeklyOt += Integer.parseInt(weeklyOtStr);
////                        }
////                    }
////                } catch (Exception e) {
////                    log.error("Could not parse rule results JSON for timesheet " + sheet.getId(), e);
////                }
////            }
////        }
////        return pastWeeklyOt;
////    }
////
////    private PayPolicyRuleResultDTO buildResult(String result, boolean success, String message) {
////        return PayPolicyRuleResultDTO.builder()
////                .ruleName(getName())
////                .result(result)
////                .success(success)
////                .message(message)
////                .build();
////    }
////}
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
//import java.util.Optional;
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
//            return buildResult("NO_WORK_TIME", true, "No gross work time to process for overtime.");
//        }
//
//        // Initialize the capped-off fact to ensure it exists
//        context.getFacts().put("weeklyOtCappedOffMinutes", 0);
//
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//
//        int potentialDailyOt = 0;
//        if (this.enableDailyOt) {
//            potentialDailyOt = calculateDailyOvertime(workedMinutes, employeeShift);
//        }
//
//        int regularHoursForWeeklyCalc = workedMinutes - potentialDailyOt;
//        int potentialWeeklyOt = 0;
//        if (enableWeeklyOt) {
//            potentialWeeklyOt = calculateWeeklyOvertime(context, regularHoursForWeeklyCalc);
//        }
//
//        // --- CONFLICT RESOLUTION LOGIC ---
//        int finalDailyOt = potentialDailyOt;
//        int finalWeeklyOt = potentialWeeklyOt;
//
//        if (potentialDailyOt > 0 && potentialWeeklyOt > 0 && dailyWeeklyOtConflict != null) {
//            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
//                log.debug("Conflict Resolution: {}. Weekly OT ({}) > 0, so Daily OT is set to 0.", dailyWeeklyOtConflict, potentialWeeklyOt);
//                finalDailyOt = 0;
//            }
//        }
//
//        // Apply caps after conflict resolution
//        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
//            finalDailyOt = Math.min(finalDailyOt, (int) (this.maxOtPerDay * 60));
//        }
//
//        context.getFacts().put("dailyOtHoursMinutes", finalDailyOt);
//        context.getFacts().put("weeklyOtHoursMinutes", finalWeeklyOt);
//
//        String message = "Identified Daily OT: " + finalDailyOt + ", Identified Weekly OT: " + finalWeeklyOt;
//        return buildResult("OT_IDENTIFIED", true, message);
//    }
//
//    private int calculateDailyOvertime(Integer grossWorkedMinutes, EmployeeShift employeeShift) {
//        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
//            return 0;
//        }
//        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift);
//        return Math.max(0, grossWorkedMinutes - thresholdInMinutes);
//    }
//
//    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
//        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) {
//                shiftDuration += 1440; // Handles overnight shifts
//            }
//            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
//        }
//        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
//    }
//
//    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayRegularMinutes) {
//        if (!this.enableWeeklyOt || this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) {
//            return 0;
//        }
//
//        String employeeId = context.getEmployeeId();
//        LocalDate workDate = context.getDate();
//        TimesheetRepository timesheetRepository = context.getTimesheetRepository();
//
//        // --- 1. Determine Week Start and Get Past Data ---
//        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
//        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
//        if (workDate.isBefore(weekStartDate)) {
//            weekStartDate = weekStartDate.minusWeeks(1);
//        }
//        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));
//
//        int pastRegularMinutes = pastWeekTimesheets.stream().mapToInt(ts -> ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0).sum();
//        int pastWeeklyOtMinutes = getPastWeeklyOtFromJson(pastWeekTimesheets);
//
//        log.debug("Weekly OT Calc for {}: pastRegular={}, pastWeeklyOt={}", workDate, pastRegularMinutes, pastWeeklyOtMinutes);
//
//        // --- 2. Determine Potential OT for Today ---
//        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
//        int totalRegularConsidered = pastRegularMinutes + currentDayRegularMinutes;
//
//        if (totalRegularConsidered <= weeklyThresholdMinutes) {
//            return 0;
//        }
//
//        int potentialOtForToday = (pastRegularMinutes >= weeklyThresholdMinutes) ? currentDayRegularMinutes : totalRegularConsidered - weeklyThresholdMinutes;
//        log.debug("Potential OT for today (uncapped): {}", potentialOtForToday);
//
//        // --- 3. Apply Weekly OT Cap & Calculate Capped Amount ---
//        int finalOtForToday = potentialOtForToday;
//        if (this.maxOtPerWeek != null && this.maxOtPerWeek > 0) {
//            int maxWeeklyOtInMinutes = (int) (this.maxOtPerWeek * 60);
//            int remainingOtCapacity = maxWeeklyOtInMinutes - pastWeeklyOtMinutes;
//            log.debug("Max weekly OT: {}, Remaining capacity: {}", maxWeeklyOtInMinutes, remainingOtCapacity);
//
//            if (remainingOtCapacity <= 0) {
//                log.debug("Weekly OT cap already met. All potential OT ({}) for today is capped.", potentialOtForToday);
//                // All potential OT is capped, none is payable as OT
//                context.getFacts().put("weeklyOtCappedOffMinutes", potentialOtForToday);
//                return 0;
//            }
//
//            finalOtForToday = Math.min(potentialOtForToday, remainingOtCapacity);
//            int cappedAmount = potentialOtForToday - finalOtForToday;
//
//            if (cappedAmount > 0) {
//                log.debug("Amount of weekly OT capped off: {}", cappedAmount);
//                context.getFacts().put("weeklyOtCappedOffMinutes", cappedAmount);
//            }
//        }
//
//        log.debug("Final weekly OT for today (capped): {}", finalOtForToday);
//        return Math.max(0, finalOtForToday);
//    }
//
//    private int getPastWeeklyOtFromJson(List<Timesheet> pastWeekTimesheets) {
//        ObjectMapper mapper = new ObjectMapper();
//        int pastWeeklyOt = 0;
//        for (Timesheet sheet : pastWeekTimesheets) {
//            if (sheet.getRuleResultsJson() != null && !sheet.getRuleResultsJson().isEmpty()) {
//                try {
//                    List<PayPolicyRuleResultDTO> results = mapper.readValue(sheet.getRuleResultsJson(), new TypeReference<List<PayPolicyRuleResultDTO>>() {});
//                    Optional<PayPolicyRuleResultDTO> otResult = results.stream().filter(r -> "OvertimeRules".equals(r.getRuleName())).findFirst();
//                    if (otResult.isPresent()) {
//                        String msg = otResult.get().getMessage();
//                        if (msg != null && msg.contains("Identified Weekly OT:")) {
//                            String weeklyOtStr = msg.substring(msg.indexOf("Identified Weekly OT:") + "Identified Weekly OT:".length()).trim();
//                            // Make parsing more robust against other text in the message
//                            weeklyOtStr = weeklyOtStr.split(",")[0].trim();
//                            pastWeeklyOt += Integer.parseInt(weeklyOtStr);
//                        }
//                    }
//                } catch (Exception e) {
//                    log.error("Could not parse rule results JSON for timesheet " + sheet.getId(), e);
//                }
//            }
//        }
//        return pastWeeklyOt;
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
            return buildResult("NO_WORK_TIME", true, "No gross work time to process for overtime.");
        }

        // Initialize the capped-off fact to ensure it exists
        context.getFacts().put("weeklyOtCappedOffMinutes", 0);

        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        int potentialDailyOt = 0;
        if (this.enableDailyOt) {
            potentialDailyOt = calculateDailyOvertime(workedMinutes, employeeShift);
        }

        int regularHoursForWeeklyCalc = workedMinutes - potentialDailyOt;
        int potentialWeeklyOt = 0;
        if (enableWeeklyOt) {
            potentialWeeklyOt = calculateWeeklyOvertime(context, regularHoursForWeeklyCalc);
        }

        // --- CONFLICT RESOLUTION LOGIC ---
        int finalDailyOt = potentialDailyOt;
        int finalWeeklyOt = potentialWeeklyOt;

        if (potentialDailyOt > 0 && potentialWeeklyOt > 0 && dailyWeeklyOtConflict != null) {
            if (dailyWeeklyOtConflict == DailyWeeklyOtConflict.EXCLUDE_DAILY_OT || dailyWeeklyOtConflict == DailyWeeklyOtConflict.PRIORITIZE_WEEKLY_OT) {
                log.debug("Conflict Resolution: {}. Weekly OT ({}) > 0, so Daily OT is set to 0.", dailyWeeklyOtConflict, potentialWeeklyOt);
                finalDailyOt = 0;
            }
        }

        // Apply caps after conflict resolution
        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            finalDailyOt = Math.min(finalDailyOt, (int) (this.maxOtPerDay * 60));
        }

        context.getFacts().put("dailyOtHoursMinutes", finalDailyOt);
        context.getFacts().put("weeklyOtHoursMinutes", finalWeeklyOt);

        String message = "Identified Daily OT: " + finalDailyOt + ", Identified Weekly OT: " + finalWeeklyOt;
        return buildResult("OT_IDENTIFIED", true, message);
    }

    private int calculateDailyOvertime(Integer grossWorkedMinutes, EmployeeShift employeeShift) {
        if (grossWorkedMinutes == null || grossWorkedMinutes <= 0) {
            return 0;
        }
        int thresholdInMinutes = getDailyThresholdInMinutes(employeeShift);
        return Math.max(0, grossWorkedMinutes - thresholdInMinutes);
    }

    private int getDailyThresholdInMinutes(EmployeeShift employeeShift) {
        if (dailyOtTrigger == DailyOtTrigger.AFTER_SHIFT_END && employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440; // Handles overnight shifts
            }
            return (int) shiftDuration + (this.gracePeriodAfterShiftEnd != null ? this.gracePeriodAfterShiftEnd : 0);
        }
        return (this.thresholdHours != null ? this.thresholdHours * 60 : 0) + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);
    }

    private int calculateWeeklyOvertime(PayPolicyExecutionContext context, int currentDayRegularMinutes) {
        if (!this.enableWeeklyOt || this.weeklyThresholdHours == null || this.weeklyThresholdHours <= 0) {
            return 0;
        }

        String employeeId = context.getEmployeeId();
        LocalDate workDate = context.getDate();
        TimesheetRepository timesheetRepository = context.getTimesheetRepository();

        // --- 1. Determine Week Start and Get Past Data ---
        WeekDay startDay = this.weeklyResetDay != null ? this.weeklyResetDay : WeekDay.MONDAY;
        LocalDate weekStartDate = workDate.with(DayOfWeek.valueOf(startDay.name()));
        if (workDate.isBefore(weekStartDate)) {
            weekStartDate = weekStartDate.minusWeeks(1);
        }
        List<Timesheet> pastWeekTimesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, weekStartDate, workDate.minusDays(1));

        int pastRegularMinutes = pastWeekTimesheets.stream().mapToInt(ts -> ts.getRegularHoursMinutes() != null ? ts.getRegularHoursMinutes() : 0).sum();
        int pastWeeklyOtMinutes = getPastWeeklyOtFromJson(pastWeekTimesheets);

        log.debug("Weekly OT Calc for {}: pastRegular={}, pastWeeklyOt={}", workDate, pastRegularMinutes, pastWeeklyOtMinutes);

        // --- 2. Determine Potential OT for Today ---
        int weeklyThresholdMinutes = this.weeklyThresholdHours * 60;
        int totalRegularConsidered = pastRegularMinutes + currentDayRegularMinutes;

        if (totalRegularConsidered <= weeklyThresholdMinutes) {
            return 0;
        }

        int potentialOtForToday = (pastRegularMinutes >= weeklyThresholdMinutes) ? currentDayRegularMinutes : totalRegularConsidered - weeklyThresholdMinutes;
        log.debug("Potential OT for today (uncapped): {}", potentialOtForToday);

        // --- 3. Apply Weekly OT Cap & Calculate Capped Amount ---
        int finalOtForToday = potentialOtForToday;
        if (this.maxOtPerWeek != null && this.maxOtPerWeek > 0) {
            int maxWeeklyOtInMinutes = (int) (this.maxOtPerWeek * 60);
            int remainingOtCapacity = maxWeeklyOtInMinutes - pastWeeklyOtMinutes;
            log.debug("Max weekly OT: {}, Remaining capacity: {}", maxWeeklyOtInMinutes, remainingOtCapacity);

            if (remainingOtCapacity <= 0) {
                log.debug("Weekly OT cap already met. All potential OT ({}) for today is capped.", potentialOtForToday);
                // All potential OT is capped, none is payable as OT
                context.getFacts().put("weeklyOtCappedOffMinutes", potentialOtForToday);
                return 0;
            }

            finalOtForToday = Math.min(potentialOtForToday, remainingOtCapacity);
            int cappedAmount = potentialOtForToday - finalOtForToday;

            if (cappedAmount > 0) {
                log.debug("Amount of weekly OT capped off: {}", cappedAmount);
                context.getFacts().put("weeklyOtCappedOffMinutes", cappedAmount);
            }
        }

        log.debug("Final weekly OT for today (capped): {}", finalOtForToday);
        return Math.max(0, finalOtForToday);
    }

    private int getPastWeeklyOtFromJson(List<Timesheet> pastWeekTimesheets) {
        ObjectMapper mapper = new ObjectMapper();
        int pastWeeklyOt = 0;
        for (Timesheet sheet : pastWeekTimesheets) {
            if (sheet.getRuleResultsJson() != null && !sheet.getRuleResultsJson().isEmpty()) {
                try {
                    List<PayPolicyRuleResultDTO> results = mapper.readValue(sheet.getRuleResultsJson(), new TypeReference<List<PayPolicyRuleResultDTO>>() {});
                    Optional<PayPolicyRuleResultDTO> otResult = results.stream().filter(r -> "OvertimeRules".equals(r.getRuleName())).findFirst();
                    if (otResult.isPresent()) {
                        String msg = otResult.get().getMessage();
                        if (msg != null && msg.contains("Identified Weekly OT:")) {
                            String weeklyOtStr = msg.substring(msg.indexOf("Identified Weekly OT:") + "Identified Weekly OT:".length()).trim();
                            // Make parsing more robust against other text in the message
                            weeklyOtStr = weeklyOtStr.split(",")[0].trim();
                            pastWeeklyOt += Integer.parseInt(weeklyOtStr);
                        }
                    }
                } catch (Exception e) {
                    log.error("Could not parse rule results JSON for timesheet " + sheet.getId(), e);
                }
            }
        }
        return pastWeeklyOt;
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
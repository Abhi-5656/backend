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
        // Evaluate if the rule is enabled and if there's any time left to process.
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        return enabled && enableDailyOt && (workedMinutes != null && workedMinutes > 0 || (Integer) context.getFact("grossWorkMinutes") > 0);
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        // Logic for unscheduled employees based on a fixed work duration
        if (employeeShift == null && dailyOtTrigger == DailyOtTrigger.AFTER_FIXED_HOURS) {
            return executeFixedDurationOt(context);
        }

        // Fallback for scheduled employees or other cases
        int minutesForOt = (int) context.getFacts().getOrDefault("workedMinutes", 0);
        context.getFacts().put("dailyOtHoursMinutes", minutesForOt);
        context.getFacts().put("workedMinutes", 0); // Consume remaining minutes

        String message = String.format("OT Calculated. Daily: %d, Weekly: 0.", minutesForOt);
        return buildResult("OT_CALCULATED", true, message);
    }

    private PayPolicyRuleResultDTO executeFixedDurationOt(PayPolicyExecutionContext context) {
        // Use the gross work minutes before any break deductions for this calculation
        int grossWorkMinutes = (int) context.getFacts().getOrDefault("grossWorkMinutes", 0);
        int dailyOtThresholdInMinutes = (thresholdHours != null ? thresholdHours * 60 : 0) + (thresholdMinutes != null ? thresholdMinutes : 0);

        int regularMinutes;
        int dailyOt;
        int excessOt = 0;

        int payableTime = grossWorkMinutes - (int) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);

        if (payableTime > dailyOtThresholdInMinutes) {
            regularMinutes = dailyOtThresholdInMinutes;
            dailyOt = payableTime - dailyOtThresholdInMinutes;
        } else {
            regularMinutes = payableTime;
            dailyOt = 0;
        }

        // Apply the cap for Max OT Per Day
        if (maxOtPerDay != null && maxOtPerDay > 0) {
            int maxOtInMinutes = (int) (maxOtPerDay * 60);
            if (dailyOt > maxOtInMinutes) {
                excessOt = dailyOt - maxOtInMinutes;
                dailyOt = maxOtInMinutes;
            }
        }

        // Update context with the final calculated values
        context.getFacts().put("finalRegularMinutes", Math.max(0, regularMinutes));
        context.getFacts().put("dailyOtHoursMinutes", dailyOt);
        context.getFacts().put("excessHoursMinutes", excessOt);
        context.getFacts().put("workedMinutes", 0); // All time has been categorized

        String message = String.format("OT Calculated. Daily: %d, Weekly: 0.", dailyOt);
        return buildResult("OT_CALCULATED", true, message);
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
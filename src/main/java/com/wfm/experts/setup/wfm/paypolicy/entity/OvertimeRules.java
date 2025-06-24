package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @ManyToMany(fetch = FetchType.EAGER) // Eager fetch for rule execution
    @JoinTable(
            name = "overtime_rules_shifts",
            joinColumns = @JoinColumn(name = "overtime_rules_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    private List<Shift> shifts;


    // --- Implement PayPolicyRule ---

    @Override
    public String getName() {
        return "OvertimeRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Rule should only run if it's enabled and there are worked minutes to evaluate
        return enabled && context.getFact("workedMinutes") != null;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // 1. Get necessary data from the context
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");

        // 2. Shift Eligibility Check
        List<Shift> eligibleShifts = getShifts();
        if (eligibleShifts != null && !eligibleShifts.isEmpty()) {
            if (employeeShift == null || employeeShift.getShift() == null) {
                return buildResult("NOT_ELIGIBLE", true, "Overtime not applicable: No shift assigned.");
            }
            boolean isEligible = eligibleShifts.stream()
                    .map(Shift::getId)
                    .anyMatch(id -> id.equals(employeeShift.getShift().getId()));
            if (!isEligible) {
                return buildResult("NOT_ELIGIBLE_SHIFT", true, "Overtime not applicable for the assigned shift.");
            }
        }

        // 3. Calculate Overtime Threshold
        int thresholdInMinutes = (this.thresholdHours != null ? this.thresholdHours * 60 : 0)
                + (this.thresholdMinutes != null ? this.thresholdMinutes : 0);

        if (workedMinutes <= thresholdInMinutes) {
            context.getFacts().put("overtimeMinutes", 0);
            return buildResult("NO_OVERTIME", true, "Work duration does not exceed overtime threshold.");
        }

        // 4. Calculate daily overtime
        int calculatedOvertime = workedMinutes - thresholdInMinutes;

        // 5. Apply daily cap
        if (this.maxOtPerDay != null && this.maxOtPerDay > 0) {
            int maxDailyMinutes = (int) (this.maxOtPerDay * 60);
            if (calculatedOvertime > maxDailyMinutes) {
                calculatedOvertime = maxDailyMinutes;
            }
        }

        // Note: Weekly and Pay Period caps would require historical data not present in the current context.
        // This would be a future enhancement requiring fetching weekly timesheet data.

        // 6. Update the context with the calculated overtime
        context.getFacts().put("overtimeMinutes", calculatedOvertime);

        return buildResult("OVERTIME_CALCULATED", true, "Calculated " + calculatedOvertime + " minutes of overtime.");
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
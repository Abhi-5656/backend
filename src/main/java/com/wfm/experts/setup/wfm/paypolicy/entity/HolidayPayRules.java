package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
import com.wfm.experts.setup.wfm.paypolicy.enums.HolidayPayType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Table(name = "holiday_pay_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayPayRules implements PayPolicyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HolidayPayType holidayPayType;

    private Double payMultiplier;
    private Integer minHoursForCompOff;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CompOffBalanceBasis maxCompOffBalanceBasis;

    private Integer maxCompOffBalance;
    private Integer compOffExpiryValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ExpiryUnit compOffExpiryUnit;

    private boolean encashOnExpiry;

    @Override
    public String getName() {
        return "HolidayPayRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        Object isHolidayFact = context.getFact("isHoliday");
        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;
        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        int unpaidBreakMinutes = (int) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);

        int holidayWorkedMinutes = 0;
        int remainingMinutes = workedMinutes;

        // If a shift exists, calculate time against the shift duration
        if (employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440; // Handles overnight shifts
            }
            int netShiftDuration = (int) shiftDuration - unpaidBreakMinutes;

            holidayWorkedMinutes = Math.min(workedMinutes, netShiftDuration);
            remainingMinutes = Math.max(0, workedMinutes - holidayWorkedMinutes);
        } else {
            // If no shift is assigned, all worked time is holiday time
            holidayWorkedMinutes = workedMinutes;
            remainingMinutes = 0;
        }

        // Update the facts in the context
        context.getFacts().put("holidayWorkedMinutes", holidayWorkedMinutes);
        context.getFacts().put("workedMinutes", remainingMinutes); // Leave the remainder for other rules (like ExcessHoursRule)

        StringBuilder message = new StringBuilder("Holiday Worked. ");
        message.append("Worked amount: ").append(holidayWorkedMinutes).append(" minutes. ");

        if (this.payMultiplier != null && this.payMultiplier > 0) {
            context.getFacts().put("payMultiplier", this.getPayMultiplier());
            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x.");
        }

        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;
            if (workedMinutes >= minMinutesForCompOff) {
                context.getFacts().put("compOffDaysEarned", 1.0);
            }
        }

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result("HOLIDAY_WORKED")
                .success(true)
                .message(message.toString().trim())
                .build();
    }
}
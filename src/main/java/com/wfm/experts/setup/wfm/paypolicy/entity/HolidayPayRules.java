//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.setup.wfm.paypolicy.enums.*;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "holiday_pay_rules")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class HolidayPayRules implements PayPolicyRule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private boolean enabled;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private HolidayPayType holidayPayType;
//
//    private Double payMultiplier;
//    private Integer minHoursForCompOff;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private CompOffBalanceBasis maxCompOffBalanceBasis;
//
//    private Integer maxCompOffBalance;
//    private Integer compOffExpiryValue;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 10)
//    private ExpiryUnit compOffExpiryUnit;
//
//    private boolean encashOnExpiry;
//
//    // ----------- PayPolicyRule implementation ---------------
//
//    @Override
//    public String getName() {
//        return "HolidayPayRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        // Rule should only run if it's enabled, the day is a holiday, and the employee has worked.
//        Object isHolidayFact = context.getFact("isHoliday");
//        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;
//
//        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
//
//        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        StringBuilder message = new StringBuilder("Holiday Pay Applied. ");
//
//        // Apply pay multiplier for hours worked on the holiday
//        if (this.payMultiplier != null && this.payMultiplier > 0) {
//            context.getFacts().put("payMultiplier", this.getPayMultiplier());
//            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x. ");
//        }
//
//        // Grant comp-off if the policy allows for it and conditions are met
//        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
//            Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
//            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;
//
//            if (workedMinutes >= minMinutesForCompOff) {
//                // Assuming 1 day of comp-off is granted for working the holiday
//                context.getFacts().put("compOffDaysEarned", 1.0);
//                message.append("Comp-off (1 day) granted.");
//            } else {
//                message.append("Comp-off not granted (worked hours below threshold).");
//            }
//        }
//
//        return PayPolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result("HOLIDAY_PAY_APPLIED")
//                .success(true)
//                .message(message.toString().trim())
//                .build();
//    }
//}
package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.*;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import jakarta.persistence.*;
import lombok.*;

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

    // ----------- PayPolicyRule implementation ---------------

    @Override
    public String getName() {
        return "HolidayPayRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Rule should only run if it's enabled, the day is a holiday, and the employee has worked.
        Object isHolidayFact = context.getFact("isHoliday");
        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;

        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");

        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        StringBuilder message = new StringBuilder("Holiday Worked. ");

        // Apply pay multiplier for hours worked on the holiday
        if (this.payMultiplier != null && this.payMultiplier > 0) {
            context.getFacts().put("payMultiplier", this.getPayMultiplier());
            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x. ");
        }

        // Grant comp-off if the policy allows for it and conditions are met
        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
            Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;

            if (workedMinutes >= minMinutesForCompOff) {
                // Assuming 1 day of comp-off is granted for working the holiday
                context.getFacts().put("compOffDaysEarned", 1.0);
                message.append("Comp-off (1 day) granted.");
            } else {
                message.append("Comp-off not granted (worked hours below threshold).");
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
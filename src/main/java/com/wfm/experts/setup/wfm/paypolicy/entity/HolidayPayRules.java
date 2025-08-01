//////package com.wfm.experts.setup.wfm.paypolicy.entity;
//////
//////import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//////import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//////import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//////import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
//////import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
//////import com.wfm.experts.setup.wfm.paypolicy.enums.HolidayPayType;
//////import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//////import jakarta.persistence.*;
//////import lombok.AllArgsConstructor;
//////import lombok.Builder;
//////import lombok.Data;
//////import lombok.NoArgsConstructor;
//////
//////import java.time.Duration;
//////
//////@Entity
//////@Table(name = "holiday_pay_rules")
//////@Data
//////@NoArgsConstructor
//////@AllArgsConstructor
//////@Builder
//////public class HolidayPayRules implements PayPolicyRule {
//////    @Id
//////    @GeneratedValue(strategy = GenerationType.IDENTITY)
//////    private Long id;
//////
//////    private boolean enabled;
//////
//////    @Enumerated(EnumType.STRING)
//////    @Column(length = 20)
//////    private HolidayPayType holidayPayType;
//////
//////    private Double payMultiplier;
//////    private Integer minHoursForCompOff;
//////
//////    @Enumerated(EnumType.STRING)
//////    @Column(length = 20)
//////    private CompOffBalanceBasis maxCompOffBalanceBasis;
//////
//////    private Integer maxCompOffBalance;
//////    private Integer compOffExpiryValue;
//////
//////    @Enumerated(EnumType.STRING)
//////    @Column(length = 10)
//////    private ExpiryUnit compOffExpiryUnit;
//////
//////    private boolean encashOnExpiry;
//////
//////    // ----------- PayPolicyRule implementation ---------------
//////
//////    @Override
//////    public String getName() {
//////        return "HolidayPayRules";
//////    }
//////
//////    @Override
//////    public boolean evaluate(PayPolicyExecutionContext context) {
//////        // Rule should only run if it's enabled, the day is a holiday, and the employee has worked.
//////        Object isHolidayFact = context.getFact("isHoliday");
//////        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;
//////
//////        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
//////
//////        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
//////    }
//////
//////    @Override
//////    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//////        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
//////        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//////
//////        int holidayWorkMinutes = workedMinutes;
//////        if (employeeShift != null && employeeShift.getShift() != null) {
//////            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//////            if (shiftDuration < 0) {
//////                shiftDuration += 1440; // Handles overnight shifts
//////            }
//////            holidayWorkMinutes = Math.min(workedMinutes, (int) shiftDuration);
//////        }
//////
//////        context.getFacts().put("holidayWorkedMinutes", holidayWorkMinutes);
//////
//////        StringBuilder message = new StringBuilder("Holiday Worked. ");
//////        message.append("Worked amount: ").append(holidayWorkMinutes).append(" minutes. ");
//////
//////        // Apply pay multiplier for hours worked on the holiday
//////        if (this.payMultiplier != null && this.payMultiplier > 0) {
//////            context.getFacts().put("payMultiplier", this.getPayMultiplier());
//////            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x. ");
//////        }
//////
//////        // Grant comp-off if the policy allows for it and conditions are met
//////        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
//////            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;
//////
//////            if (workedMinutes >= minMinutesForCompOff) {
//////                // Assuming 1 day of comp-off is granted for working the holiday
//////                context.getFacts().put("compOffDaysEarned", 1.0);
//////                message.append("Comp-off (1 day) granted.");
//////            } else {
//////                message.append("Comp-off not granted (worked hours below threshold).");
//////            }
//////        }
//////
//////        return PayPolicyRuleResultDTO.builder()
//////                .ruleName(getName())
//////                .result("HOLIDAY_WORKED")
//////                .success(true)
//////                .message(message.toString().trim())
//////                .build();
//////    }
//////}
////// harshwfm/wfm-backend/HarshWfm-wfm-backend-0668c2132deb2960bc35069c452ecfc4ad1fd48b/src/main/java/com/wfm/experts/setup/wfm/paypolicy/entity/HolidayPayRules.java
////package com.wfm.experts.setup.wfm.paypolicy.entity;
////
////import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
////import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
////import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
////import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
////import com.wfm.experts.setup.wfm.paypolicy.enums.HolidayPayType;
////import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
////import jakarta.persistence.*;
////import lombok.AllArgsConstructor;
////import lombok.Builder;
////import lombok.Data;
////import lombok.NoArgsConstructor;
////
////@Entity
////@Table(name = "holiday_pay_rules")
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////@Builder
////public class HolidayPayRules implements PayPolicyRule {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    private boolean enabled;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private HolidayPayType holidayPayType;
////
////    private Double payMultiplier;
////    private Integer minHoursForCompOff;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 20)
////    private CompOffBalanceBasis maxCompOffBalanceBasis;
////
////    private Integer maxCompOffBalance;
////    private Integer compOffExpiryValue;
////
////    @Enumerated(EnumType.STRING)
////    @Column(length = 10)
////    private ExpiryUnit compOffExpiryUnit;
////
////    private boolean encashOnExpiry;
////
////    @Override
////    public String getName() {
////        return "HolidayPayRules";
////    }
////
////    @Override
////    public boolean evaluate(PayPolicyExecutionContext context) {
////        Object isHolidayFact = context.getFact("isHoliday");
////        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;
////        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
////        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
////    }
////
////    @Override
////    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
////        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
////
////        // Dynamically get the daily OT threshold from OvertimeRules configuration
////        int dailyThreshold = 540; // Default to 9 hours (540 minutes) as a fallback.
////        PayPolicy payPolicy = context.getPayPolicy();
////        if (payPolicy != null && payPolicy.getOvertimeRules() != null) {
////            OvertimeRules otRules = payPolicy.getOvertimeRules();
////            if (otRules.getDailyOtTrigger() == com.wfm.experts.setup.wfm.paypolicy.enums.DailyOtTrigger.AFTER_FIXED_HOURS) {
////                int thresholdHours = otRules.getThresholdHours() != null ? otRules.getThresholdHours() : 0;
////                int thresholdMinutes = otRules.getThresholdMinutes() != null ? otRules.getThresholdMinutes() : 0;
////                dailyThreshold = (thresholdHours * 60) + thresholdMinutes;
////            }
////        }
////
////        int holidayWorkedMinutes = Math.min(workedMinutes, dailyThreshold);
////
////        // Set the holiday work bucket for the final timesheet.
////        context.getFacts().put("holidayWorkedMinutes", holidayWorkedMinutes);
////
////        // **FIX**: Do NOT reduce the workedMinutes for the next rules in the chain.
////        // This allows the OvertimeRule to see the full duration of work.
////
////        StringBuilder message = new StringBuilder("Holiday Worked. ");
////        message.append("Worked amount: ").append(holidayWorkedMinutes).append(" minutes. ");
////
////        if (this.payMultiplier != null && this.payMultiplier > 0) {
////            context.getFacts().put("payMultiplier", this.getPayMultiplier());
////            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x.");
////        }
////
////        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
////            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;
////            if (workedMinutes >= minMinutesForCompOff) {
////                context.getFacts().put("compOffDaysEarned", 1.0);
////            }
////        }
////
////        return PayPolicyRuleResultDTO.builder()
////                .ruleName(getName())
////                .result("HOLIDAY_WORKED")
////                .success(true)
////                .message(message.toString().trim())
////                .build();
////    }
////}
//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.CompOffBalanceBasis;
//import com.wfm.experts.setup.wfm.paypolicy.enums.ExpiryUnit;
//import com.wfm.experts.setup.wfm.paypolicy.enums.HolidayPayType;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.Duration;
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
//    @Override
//    public String getName() {
//        return "HolidayPayRules";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        Object isHolidayFact = context.getFact("isHoliday");
//        boolean isHoliday = (isHolidayFact instanceof Boolean) && (Boolean) isHolidayFact;
//        Integer workedMinutes = (Integer) context.getFacts().get("workedMinutes");
//        return enabled && isHoliday && workedMinutes != null && workedMinutes > 0;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        Integer workedMinutes = (Integer) context.getFact("workedMinutes");
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//
//        int holidayWorkedMinutes = workedMinutes;
//
//        // If a shift exists, cap the holiday work minutes at the scheduled shift duration.
//        if (employeeShift != null && employeeShift.getShift() != null) {
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) {
//                shiftDuration += 1440; // Handles overnight shifts
//            }
//            holidayWorkedMinutes = Math.min(workedMinutes, (int) shiftDuration);
//        }
//
//        // Set the holiday work bucket for the final timesheet.
//        context.getFacts().put("holidayWorkedMinutes", holidayWorkedMinutes);
//
//        // Do NOT reduce the workedMinutes for the next rules in the chain.
//        // This allows the OvertimeRule to see the full duration of work.
//
//        StringBuilder message = new StringBuilder("Holiday Worked. ");
//        message.append("Worked amount: ").append(holidayWorkedMinutes).append(" minutes. ");
//
//        if (this.payMultiplier != null && this.payMultiplier > 0) {
//            context.getFacts().put("payMultiplier", this.getPayMultiplier());
//            message.append("Pay multiplier set to ").append(this.payMultiplier).append("x.");
//        }
//
//        if (this.holidayPayType == HolidayPayType.PAID_AND_COMP_OFF) {
//            int minMinutesForCompOff = (this.minHoursForCompOff != null ? this.minHoursForCompOff : 0) * 60;
//            if (workedMinutes >= minMinutesForCompOff) {
//                context.getFacts().put("compOffDaysEarned", 1.0);
//            }
//        }
//
//        return PayPolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result("HOLIDAY_WORKED")
//                .success(true)
//                .message(message.toString().trim())
//                .build();
//    }
//}
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

        int holidayWorkedMinutes = workedMinutes;

        if (employeeShift != null && employeeShift.getShift() != null) {
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) {
                shiftDuration += 1440;
            }
            holidayWorkedMinutes = Math.min(workedMinutes, (int) shiftDuration);
        }

        context.getFacts().put("holidayWorkedMinutes", holidayWorkedMinutes);

        // *** THIS IS THE FIX: Reduce workedMinutes for the next rules in the chain ***
        context.getFacts().put("workedMinutes", workedMinutes - holidayWorkedMinutes);

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
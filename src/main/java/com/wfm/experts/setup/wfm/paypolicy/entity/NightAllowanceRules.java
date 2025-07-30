//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.Comparator;
//import java.util.List;
//
//@Entity
//@Table(name = "night_allowance_rules")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class NightAllowanceRules implements PayPolicyRule {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private boolean enabled;
//
//    @Column(name = "start_time", length = 10)
//    private String startTime;
//
//    @Column(name = "end_time", length = 10)
//    private String endTime;
//
//    @Column(name = "pay_multiplier")
//    private Double payMultiplier;
//
//    @Override
//    public String getName() {
//        return "NightAllowanceRule";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        return enabled;
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        Integer totalWorkMinutes = (Integer) context.getFact("workedMinutes");
//        if (totalWorkMinutes == null || totalWorkMinutes <= 0) {
//            return buildResult("NO_WORK_TIME", true, "No work time for night allowance calculation.");
//        }
//
//        Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
//        int netPayableMinutes = totalWorkMinutes - unpaidBreakMinutes;
//
//        List<PunchEvent> punches = context.getPunchEvents();
//        if (punches == null || punches.size() < 2) {
//            return buildResult("INSUFFICIENT_PUNCHES", true, "Not enough punch events.");
//        }
//
//        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
//        LocalDateTime workStart = punches.get(0).getEventTime();
//        LocalDateTime workEnd = punches.get(punches.size() - 1).getEventTime();
//
//        LocalTime nightStart = LocalTime.parse(startTime);
//        LocalTime nightEnd = LocalTime.parse(endTime);
//
//        long grossNightOverlap = calculateNightMinutes(workStart, workEnd, nightStart, nightEnd);
//
//        if (grossNightOverlap <= 0) {
//            return buildResult("NO_NIGHT_OVERLAP", true, "Work did not overlap with the night allowance period.");
//        }
//
//        int finalNightMinutes;
//        EmployeeShift currentShift = (EmployeeShift) context.getFact("shift");
//
//        if (currentShift == null) {
//            // UNSCHEDULED LOGIC: If any part of the work is at night, the entire paid duration is night work.
//            finalNightMinutes = netPayableMinutes;
//        } else {
//            // SCHEDULED LOGIC: Only the actual paid time within the night window is night work.
//            finalNightMinutes = (int) Math.max(0, grossNightOverlap - unpaidBreakMinutes);
//        }
//
//        if (finalNightMinutes <= 0) {
//            return buildResult("NO_NIGHT_WORK", true, "No paid work occurred during the night allowance period.");
//        }
//
//        context.getFacts().put("nightWorkedMinutes", finalNightMinutes);
//
//        String message = String.format(
//                "Applied night allowance for %d minutes. Worked Minutes: %d, Pay Multiplier: %s",
//                finalNightMinutes,
//                finalNightMinutes,
//                payMultiplier
//        );
//
//        return buildResult("NIGHT_WORKED", true, message);
//    }
//
//    private long calculateNightMinutes(LocalDateTime workStart, LocalDateTime workEnd, LocalTime nightStart, LocalTime nightEnd) {
//        long totalNightMinutes = 0;
//        LocalDateTime current = workStart;
//        while (current.isBefore(workEnd)) {
//            if (isWithinNightWindow(current.toLocalTime(), nightStart, nightEnd)) {
//                totalNightMinutes++;
//            }
//            current = current.plusMinutes(1);
//        }
//        return totalNightMinutes;
//    }
//
//    private boolean isWithinNightWindow(LocalTime time, LocalTime nightStart, LocalTime nightEnd) {
//        if (nightStart.isBefore(nightEnd)) {
//            return !time.isBefore(nightStart) && time.isBefore(nightEnd);
//        } else {
//            return !time.isBefore(nightStart) || time.isBefore(nightEnd);
//        }
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

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "night_allowance_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NightAllowanceRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Column(name = "start_time", length = 10)
    private String startTime;

    @Column(name = "end_time", length = 10)
    private String endTime;

    @Column(name = "pay_multiplier")
    private Double payMultiplier;

    @Override
    public String getName() {
        return "NightAllowanceRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        List<PunchEvent> punches = context.getPunchEvents();
        if (punches == null || punches.size() < 2) {
            return buildResult("INSUFFICIENT_PUNCHES", true, "Not enough punch events.");
        }

        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
        LocalDateTime workStart = punches.get(0).getEventTime();
        LocalDateTime workEnd = punches.get(punches.size() - 1).getEventTime();

        LocalTime nightStart = LocalTime.parse(startTime);
        LocalTime nightEnd = LocalTime.parse(endTime);

        long grossNightMinutes = calculateNightMinutes(workStart, workEnd, nightStart, nightEnd);

        if (grossNightMinutes <= 0) {
            return buildResult("NO_NIGHT_OVERLAP", true, "Work did not overlap with the night allowance period.");
        }

        Integer unpaidBreakMinutes = (Integer) context.getFacts().getOrDefault("unpaidBreakMinutes", 0);
        int finalPaidNightMinutes = (int) Math.max(0, grossNightMinutes - unpaidBreakMinutes);

        if (finalPaidNightMinutes <= 0) {
            return buildResult("NO_NIGHT_WORK", true, "No paid work occurred during the night allowance period.");
        }

        context.getFacts().put("nightWorkedMinutes", finalPaidNightMinutes);

        String message = String.format(
                "Applied night allowance for %d minutes. Worked Minutes: %d, Pay Multiplier: %s",
                finalPaidNightMinutes,
                finalPaidNightMinutes,
                payMultiplier
        );

        return buildResult("NIGHT_WORKED", true, message);
    }

    private long calculateNightMinutes(LocalDateTime workStart, LocalDateTime workEnd, LocalTime nightStart, LocalTime nightEnd) {
        long totalNightMinutes = 0;
        LocalDateTime current = workStart;
        while (current.isBefore(workEnd)) {
            if (isWithinNightWindow(current.toLocalTime(), nightStart, nightEnd)) {
                totalNightMinutes++;
            }
            current = current.plusMinutes(1);
        }
        return totalNightMinutes;
    }

    private boolean isWithinNightWindow(LocalTime time, LocalTime nightStart, LocalTime nightEnd) {
        if (nightStart.isBefore(nightEnd)) {
            return !time.isBefore(nightStart) && time.isBefore(nightEnd);
        } else {
            return !time.isBefore(nightStart) || time.isBefore(nightEnd);
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
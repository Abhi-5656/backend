//package com.wfm.experts.setup.wfm.paypolicy.entity;
//
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceRuleMode;
//import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceStatus;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.Duration;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Entity
//@Table(name = "attendance_rules")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class AttendanceRule implements PayPolicyRule {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ElementCollection(targetClass = AttendanceRuleMode.class, fetch = FetchType.EAGER)
//    @CollectionTable(name = "attendance_rule_modes", joinColumns = @JoinColumn(name = "rule_id"))
//    @Enumerated(EnumType.STRING)
//    @Column(name = "mode", nullable = false)
//    private Set<AttendanceRuleMode> enabledModes = new HashSet<>();
//
//    // -- Unscheduled Settings --
//    @Column(name = "unscheduled_full_day_hours")
//    private Integer unscheduledFullDayHours;
//
//    @Column(name = "unscheduled_full_day_minutes")
//    private Integer unscheduledFullDayMinutes;
//
//    @Column(name = "unscheduled_half_day_hours")
//    private Integer unscheduledHalfDayHours;
//
//    @Column(name = "unscheduled_half_day_minutes")
//    private Integer unscheduledHalfDayMinutes;
//
//    // -- Scheduled Settings --
//    @Column(name = "scheduled_full_day_percentage")
//    private Integer scheduledFullDayPercentage;
//
//    @Column(name = "scheduled_half_day_percentage")
//    private Integer scheduledHalfDayPercentage;
//
//    @Override
//    public String getName() {
//        return "AttendanceRule";
//    }
//
//    @Override
//    public boolean evaluate(PayPolicyExecutionContext context) {
//        return enabledModes != null && !enabledModes.isEmpty();
//    }
//
//    @Override
//    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
//        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
//        boolean isScheduled = employeeShift != null && employeeShift.getShift() != null;
//
//        AttendanceRuleMode effectiveMode = isScheduled ? AttendanceRuleMode.SCHEDULED : AttendanceRuleMode.UNSCHEDULED;
//
//        if (!enabledModes.contains(effectiveMode)) {
//            return buildResult(AttendanceStatus.ABSENT, "Attendance rule for " + effectiveMode.name().toLowerCase() + " employees is not enabled.");
//        }
//
//        List<PunchEvent> punches = context.getPunchEvents();
//        if (punches == null || punches.isEmpty()) {
//            return buildResult(AttendanceStatus.ABSENT, "No punch events found.");
//        }
//        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
//
//        PunchEvent inPunch = punches.stream().filter(p -> p.getPunchType() == PunchType.IN).findFirst().orElse(null);
//        PunchEvent outPunch = punches.stream().filter(p -> p.getPunchType() == PunchType.OUT).reduce((first, second) -> second).orElse(null);
//
//        if (inPunch == null || outPunch == null) {
//            return buildResult(AttendanceStatus.ABSENT, "Missing IN or OUT punch.");
//        }
//
//        long workMinutes = Duration.between(inPunch.getEventTime(), outPunch.getEventTime()).toMinutes();
//        long fullDayThreshold;
//        long halfDayThreshold;
//        String ruleTypeMessage;
//
//        if (isScheduled) {
//            ruleTypeMessage = "Scheduled rule applied. ";
//            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
//            if (shiftDuration < 0) shiftDuration += 1440;
//
//            fullDayThreshold = (long) (shiftDuration * ((this.scheduledFullDayPercentage != null ? this.scheduledFullDayPercentage : 100) / 100.0));
//            halfDayThreshold = (long) (shiftDuration * ((this.scheduledHalfDayPercentage != null ? this.scheduledHalfDayPercentage : 50) / 100.0));
//        } else {
//            ruleTypeMessage = "Unscheduled rule applied. ";
//            fullDayThreshold = (long) (this.unscheduledFullDayHours != null ? this.unscheduledFullDayHours : 8) * 60 + (this.unscheduledFullDayMinutes != null ? this.unscheduledFullDayMinutes : 0);
//            halfDayThreshold = (long) (this.unscheduledHalfDayHours != null ? this.unscheduledHalfDayHours : 4) * 60 + (this.unscheduledHalfDayMinutes != null ? this.unscheduledHalfDayMinutes : 0);
//        }
//
//        if (workMinutes >= fullDayThreshold) {
//            return buildResult(AttendanceStatus.PRESENT, ruleTypeMessage + "Worked " + workMinutes + " minutes.");
//        } else if (workMinutes >= halfDayThreshold) {
//            return buildResult(AttendanceStatus.HALF_DAY, ruleTypeMessage + "Worked " + workMinutes + " minutes.");
//        } else {
//            return buildResult(AttendanceStatus.ABSENT, ruleTypeMessage + "Work duration (" + workMinutes + " min) is less than the half day threshold.");
//        }
//    }
//
//    private PayPolicyRuleResultDTO buildResult(AttendanceStatus status, String messageDetails) {
//        String finalMessage = "Status: " + status.name() + ". " + messageDetails;
//        return PayPolicyRuleResultDTO.builder()
//                .ruleName(getName())
//                .result(status.name())
//                .success(true)
//                .message(finalMessage)
//                .build();
//    }
//}
package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceRuleMode;
import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceStatus;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "attendance_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRule implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = AttendanceRuleMode.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "attendance_rule_modes", joinColumns = @JoinColumn(name = "rule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private Set<AttendanceRuleMode> enabledModes = new HashSet<>();

    // -- Unscheduled Settings --
    @Column(name = "unscheduled_full_day_hours")
    private Integer unscheduledFullDayHours;

    @Column(name = "unscheduled_full_day_minutes")
    private Integer unscheduledFullDayMinutes;

    @Column(name = "unscheduled_half_day_hours")
    private Integer unscheduledHalfDayHours;

    @Column(name = "unscheduled_half_day_minutes")
    private Integer unscheduledHalfDayMinutes;

    // -- Scheduled Settings --
    @Column(name = "scheduled_full_day_percentage")
    private Integer scheduledFullDayPercentage;

    @Column(name = "scheduled_half_day_percentage")
    private Integer scheduledHalfDayPercentage;

    @Override
    public String getName() {
        return "AttendanceRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabledModes != null && !enabledModes.isEmpty();
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        EmployeeShift employeeShift = (EmployeeShift) context.getFact("shift");
        boolean isScheduled = employeeShift != null && employeeShift.getShift() != null;

        AttendanceRuleMode effectiveMode = isScheduled ? AttendanceRuleMode.SCHEDULED : AttendanceRuleMode.UNSCHEDULED;

        if (!enabledModes.contains(effectiveMode)) {
            return buildResult(AttendanceStatus.ABSENT, "Attendance rule for " + effectiveMode.name().toLowerCase() + " employees is not enabled.");
        }

        List<PunchEvent> punches = context.getPunchEvents();
        if (punches == null || punches.isEmpty()) {
            return buildResult(AttendanceStatus.ABSENT, "No punch events found.");
        }
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));

        PunchEvent inPunch = punches.stream().filter(p -> p.getPunchType() == PunchType.IN).findFirst().orElse(null);
        PunchEvent outPunch = punches.stream().filter(p -> p.getPunchType() == PunchType.OUT).reduce((first, second) -> second).orElse(null);

        if (inPunch == null || outPunch == null) {
            return buildResult(AttendanceStatus.PENDING, "Missing IN or OUT punch.");
        }

        long workMinutes = Duration.between(inPunch.getEventTime(), outPunch.getEventTime()).toMinutes();
        long fullDayThreshold;
        long halfDayThreshold;
        String ruleTypeMessage;

        if (isScheduled) {
            ruleTypeMessage = "Scheduled rule applied. ";
            long shiftDuration = Duration.between(employeeShift.getShift().getStartTime(), employeeShift.getShift().getEndTime()).toMinutes();
            if (shiftDuration < 0) shiftDuration += 1440;

            fullDayThreshold = (long) (shiftDuration * ((this.scheduledFullDayPercentage != null ? this.scheduledFullDayPercentage : 100) / 100.0));
            halfDayThreshold = (long) (shiftDuration * ((this.scheduledHalfDayPercentage != null ? this.scheduledHalfDayPercentage : 50) / 100.0));
        } else {
            ruleTypeMessage = "Unscheduled rule applied. ";
            fullDayThreshold = (long) (this.unscheduledFullDayHours != null ? this.unscheduledFullDayHours : 8) * 60 + (this.unscheduledFullDayMinutes != null ? this.unscheduledFullDayMinutes : 0);
            halfDayThreshold = (long) (this.unscheduledHalfDayHours != null ? this.unscheduledHalfDayHours : 4) * 60 + (this.unscheduledHalfDayMinutes != null ? this.unscheduledHalfDayMinutes : 0);
        }

        if (workMinutes >= fullDayThreshold) {
            return buildResult(AttendanceStatus.PRESENT, ruleTypeMessage + "Worked " + workMinutes + " minutes.");
        } else if (workMinutes >= halfDayThreshold) {
            return buildResult(AttendanceStatus.HALF_DAY, ruleTypeMessage + "Worked " + workMinutes + " minutes.");
        } else {
            return buildResult(AttendanceStatus.ABSENT, ruleTypeMessage + "Work duration (" + workMinutes + " min) is less than the half day threshold.");
        }
    }

    private PayPolicyRuleResultDTO buildResult(AttendanceStatus status, String messageDetails) {
        String finalMessage = "Status: " + status.name() + ". " + messageDetails;
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(status.name())
                .success(true)
                .message(finalMessage)
                .build();
    }
}
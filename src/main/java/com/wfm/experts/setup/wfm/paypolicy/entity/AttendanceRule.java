package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.enums.AttendanceStatus; // Import the new enum
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "full_day_hours")
    private Integer fullDayHours;     // e.g. 8

    @Column(name = "full_day_minutes")
    private Integer fullDayMinutes;   // e.g. 0

    @Column(name = "half_day_hours")
    private Integer halfDayHours;     // e.g. 4

    @Column(name = "half_day_minutes")
    private Integer halfDayMinutes;   // e.g. 0

    // --- Implement interface methods ---

    @Override
    public String getName() {
        return "AttendanceRule";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        // Simple check: Only run if enabled
        return enabled;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        // Get all punches for the work date and sort them by time
        List<PunchEvent> punches = context.getPunchEvents();
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));

        // Find the first IN punch
        PunchEvent inPunch = punches.stream()
                .filter(p -> p.getPunchType() == PunchType.IN)
                .findFirst()
                .orElse(null);

        // Find the last OUT punch
        PunchEvent outPunch = punches.stream()
                .filter(p -> p.getPunchType() == PunchType.OUT)
                .reduce((first, second) -> second)
                .orElse(null);

        // Use the enum for type safety and standard constants. Default to ABSENT.
        AttendanceStatus attendanceStatus = AttendanceStatus.ABSENT;
        String messageDetails;

        // Calculate thresholds in total minutes
        int fullDayThreshold = Optional.ofNullable(context.getPayPolicy().getAttendanceRule())
                .map(r -> (r.getFullDayHours() != null ? r.getFullDayHours() : 0) * 60 +
                        (r.getFullDayMinutes() != null ? r.getFullDayMinutes() : 0))
                .orElse(480); // Fallback: 8 hours

        int halfDayThreshold = Optional.ofNullable(context.getPayPolicy().getAttendanceRule())
                .map(r -> (r.getHalfDayHours() != null ? r.getHalfDayHours() : 0) * 60 +
                        (r.getHalfDayMinutes() != null ? r.getHalfDayMinutes() : 0))
                .orElse(240); // Fallback: 4 hours

        if (inPunch != null && outPunch != null) {
            long workMinutes = Duration.between(inPunch.getEventTime(), outPunch.getEventTime()).toMinutes();

            if (workMinutes >= fullDayThreshold) {
                attendanceStatus = AttendanceStatus.PRESENT;
                messageDetails = "Worked " + workMinutes + " minutes.";
            } else if (workMinutes >= halfDayThreshold) {
                attendanceStatus = AttendanceStatus.HALF_DAY;
                messageDetails = "Worked " + workMinutes + " minutes.";
            } else {
                attendanceStatus = AttendanceStatus.ABSENT;
                messageDetails = "Work duration (" + workMinutes + " min) is less than the threshold for a half day.";
            }
        } else {
            // The status is already ABSENT by default
            messageDetails = "Missing IN or OUT punch.";
        }

        // Construct the final message
        String finalMessage = "Status: " + attendanceStatus.name() + ". " + messageDetails;

        // Build the result DTO using the enum's string representation
        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(attendanceStatus.name()) // .name() returns the constant as a String (e.g., "PRESENT")
                .success(true)
                .message(finalMessage)
                .build();
    }
}

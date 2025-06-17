package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "punch_event_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEventRules implements PayPolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private Integer earlyIn;
    private Integer lateIn;
    private Integer earlyOut;
    private Integer lateOut;
    private boolean notifyOnPunchEvents;

    // --- PayPolicyRule interface implementation ---

    @Override
    public String getName() {
        return "PunchEventRules";
    }

    @Override
    public boolean evaluate(PayPolicyExecutionContext context) {
        return enabled && context.getFact("shift") instanceof EmployeeShift;
    }

    @Override
    public PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context) {
        EmployeeShift shift = (EmployeeShift) context.getFact("shift");
        if (shift == null || shift.getShift() == null) {
            return PayPolicyRuleResultDTO.builder().ruleName(getName()).result("NO_SHIFT").success(false).message("No scheduled shift found for this day.").build();
        }

        LocalTime shiftStartTime = shift.getShift().getStartTime();
        LocalTime shiftEndTime = shift.getShift().getEndTime();
        List<PunchEvent> punches = context.getPunchEvents();
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));

        StringBuilder violations = new StringBuilder();

        // Check first IN punch
        punches.stream().filter(p -> p.getPunchType() == PunchType.IN).findFirst().ifPresent(inPunch -> {
            LocalTime punchTime = inPunch.getEventTime().toLocalTime();
            long diffMinutes = Duration.between(shiftStartTime, punchTime).toMinutes();

            if (diffMinutes > lateIn) {
                violations.append(String.format("Late In: Punched in %d minutes after shift start. ", diffMinutes));
            } else if (diffMinutes < -earlyIn) {
                violations.append(String.format("Early In: Punched in %d minutes before shift start. ", -diffMinutes));
            }
        });

        // Check last OUT punch
        punches.stream().filter(p -> p.getPunchType() == PunchType.OUT).reduce((first, second) -> second).ifPresent(outPunch -> {
            LocalTime punchTime = outPunch.getEventTime().toLocalTime();
            long diffMinutes = Duration.between(shiftEndTime, punchTime).toMinutes();

            if (diffMinutes > lateOut) {
                violations.append(String.format("Late Out: Punched out %d minutes after shift end. ", diffMinutes));
            } else if (diffMinutes < -earlyOut) {
                violations.append(String.format("Early Out: Punched out %d minutes before shift end. ", -diffMinutes));
            }
        });

        String resultMessage = violations.length() > 0 ? violations.toString().trim() : "All punches are within grace periods.";

        return PayPolicyRuleResultDTO.builder()
                .ruleName(getName())
                .result(violations.length() > 0 ? "VIOLATION" : "OK")
                .success(true)
                .message(resultMessage)
                .build();
    }
}

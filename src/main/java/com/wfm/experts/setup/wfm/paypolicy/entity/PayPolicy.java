package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pay_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_name", nullable = false, unique = true, length = 100)
    private String policyName;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "use_filo_calculation")
    private Boolean useFiloCalculation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rounding_rules_id")
    private RoundingRules roundingRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "punch_event_rules_id")
    private PunchEventRules punchEventRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "break_rules_id")
    private BreakRules breakRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "overtime_rules_id")
    private OvertimeRules overtimeRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "night_allowance_rules_id")
    private NightAllowanceRules nightAllowanceRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pay_period_rules_id")
    private PayPeriodRules payPeriodRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "holiday_pay_rules_id")
    private HolidayPayRules holidayPayRules;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attendance_rule_id")
    private AttendanceRule attendanceRule;

    /**
     * Return all rules in a list for rule engine execution
     * Only non-null rules are included
     */
    public List<PayPolicyRule> getRules() {
        List<PayPolicyRule> rules = new ArrayList<>();
        if (attendanceRule != null) rules.add(attendanceRule);
        if (roundingRules != null) rules.add(roundingRules);
        if (punchEventRules != null) rules.add(punchEventRules);
        if (breakRules != null) rules.add(breakRules);
        if (overtimeRules != null) rules.add(overtimeRules);
        if (payPeriodRules != null) rules.add(payPeriodRules);
        if (holidayPayRules != null) rules.add(holidayPayRules);
        return rules;
    }
}
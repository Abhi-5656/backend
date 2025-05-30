package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
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
    @JoinColumn(name = "pay_period_rules_id")
    private PayPeriodRules payPeriodRules;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "holiday_pay_rules_id")
    private HolidayPayRules holidayPayRules;

    @ManyToMany
    @JoinTable(
            name = "pay_policy_shifts",
            joinColumns = @JoinColumn(name = "pay_policy_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    private List<Shift> shifts;
}

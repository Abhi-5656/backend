package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.PayCalculationType;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "pay_period_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPeriodRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PayCalculationType periodType;

    @Column(length = 10)
    private String referenceDate; // yyyy-MM-dd

    @Column(length = 10)
    private String weekStart;     // "SUNDAY" or "MONDAY"

    @ElementCollection
    @CollectionTable(name = "pay_period_semi_monthly_days", joinColumns = @JoinColumn(name = "pay_period_rules_id"))
    @Column(name = "day")
    private List<Integer> semiMonthlyDays;
}

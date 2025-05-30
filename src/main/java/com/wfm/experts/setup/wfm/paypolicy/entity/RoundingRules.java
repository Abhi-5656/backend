package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingRuleScope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rounding_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoundingRuleScope scope;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "clock_in_rule_id")
    private RoundingRule clockIn;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "clock_out_rule_id")
    private RoundingRule clockOut;
}

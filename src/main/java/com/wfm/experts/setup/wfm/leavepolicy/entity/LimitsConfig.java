package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_limits_config")
@Getter
@Setter
public class LimitsConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "carry_forward_config_id", referencedColumnName = "id")
    private CarryForwardConfig carryForward;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "encashment_config_id", referencedColumnName = "id")
    private EncashmentConfig encashment;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "eligibility_config_id", referencedColumnName = "id")
    private EligibilityConfig eligibility;
}
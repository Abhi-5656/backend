package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.CarryForwardType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_carry_forward_limits_config")
@Getter
@Setter
public class CarryForwardLimitsConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    private Integer cap;

    @Enumerated(EnumType.STRING)
    private CarryForwardType capType;

    // expiryInDays is removed
}
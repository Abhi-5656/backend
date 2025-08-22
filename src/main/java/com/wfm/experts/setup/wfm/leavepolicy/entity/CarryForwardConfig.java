package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.CarryForwardType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_carry_forward_config")
@Getter
@Setter
public class CarryForwardConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cap;

    @Enumerated(EnumType.STRING)
    private CarryForwardType capType;

    private Integer expiryInDays;
}
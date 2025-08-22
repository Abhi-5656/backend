package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_encashment_config")
@Getter
@Setter
public class EncashmentConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxEncashableDays;
}
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_proration_config")
@Getter
@Setter
public class ProrationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isEnabled;
    private Integer cutoffDay;
    private Integer grantPercentageBeforeCutoff;
    private Integer grantPercentageAfterCutoff;
}
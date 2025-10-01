// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/EarnedGrantConfig.java
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.enums.PostingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_earned_grant_config")
@Getter
@Setter
public class EarnedGrantConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double maxDaysPerYear;
    private Double maxDaysPerMonth; // Added field
    private Double maxDaysPerPayPeriod; // Added field
    private Double ratePerPeriod;
    private Integer maxConsecutiveDays;

    @Enumerated(EnumType.STRING)
    private GrantPeriod grantPeriod; // Added field

    @Enumerated(EnumType.STRING)
    private PostingType posting;

    private Integer minAdvanceNoticeInDays;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "proration_config_id", referencedColumnName = "id")
    private ProrationConfig prorationConfig;
}
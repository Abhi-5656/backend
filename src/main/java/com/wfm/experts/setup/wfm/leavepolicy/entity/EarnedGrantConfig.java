package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.AccrualCadence;
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

    private Integer maxDaysPerYear;
    private Double ratePerPeriod;
    private Integer maxConsecutiveDays;

    @Enumerated(EnumType.STRING)
    private AccrualCadence accrualCadence;

    @Enumerated(EnumType.STRING)
    private PostingType posting;

    private Integer minAdvanceNoticeInDays;
}
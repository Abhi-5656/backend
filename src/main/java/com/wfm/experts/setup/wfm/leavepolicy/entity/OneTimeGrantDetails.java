package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_one_time_grant_details")
@Getter
@Setter
public class OneTimeGrantDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxDays;
    private Integer minAdvanceNoticeInDays;
    private Integer minWorkedBeforeGrantInDays;
}
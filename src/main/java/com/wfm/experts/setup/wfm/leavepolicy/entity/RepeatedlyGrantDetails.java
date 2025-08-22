package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_repeatedly_grant_details")
@Getter
@Setter
public class RepeatedlyGrantDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxDaysPerYear;
    private Integer maxDaysPerMonth;
    private Integer minAdvanceNoticeInDays;
    private Integer minWorkedBeforeGrantInDays;
}
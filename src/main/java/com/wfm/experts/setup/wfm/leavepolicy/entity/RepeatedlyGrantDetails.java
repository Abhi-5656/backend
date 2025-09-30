// Save as: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/RepeatedlyGrantDetails.java
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantPeriod;
import com.wfm.experts.setup.wfm.leavepolicy.enums.PostingType;
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
    private Integer maxDaysPerPayPeriod; // Added field

    @Enumerated(EnumType.STRING)
    private GrantPeriod grantPeriod; // Added field

    @Enumerated(EnumType.STRING)
    private PostingType posting; // Added field

    private Integer minWorkedBeforeGrantInDays;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "proration_config_id", referencedColumnName = "id")
    private ProrationConfig prorationConfig;
}
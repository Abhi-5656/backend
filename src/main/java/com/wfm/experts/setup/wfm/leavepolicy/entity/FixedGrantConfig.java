package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantFrequency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_fixed_grant_config")
@Getter
@Setter
public class FixedGrantConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GrantFrequency frequency;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "one_time_details_id", referencedColumnName = "id")
    private OneTimeGrantDetails oneTimeDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "repeatedly_details_id", referencedColumnName = "id")
    private RepeatedlyGrantDetails repeatedlyDetails;
}
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantExpiration;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_grants_config")
@Getter
@Setter
public class GrantsConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private GrantType grantType;

    @Enumerated(EnumType.STRING)
    private GrantExpiration expiration;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fixed_grant_id", referencedColumnName = "id")
    private FixedGrantConfig fixedGrant;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "earned_grant_id", referencedColumnName = "id")
    private EarnedGrantConfig earnedGrant;
}
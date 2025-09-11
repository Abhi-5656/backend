package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "leave_profile")
@Getter
@Setter
public class LeaveProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_name", nullable = false, unique = true)
    private String profileName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "leave_profile_policies",
            joinColumns = @JoinColumn(name = "leave_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "leave_policy_id")
    )
    private Set<LeavePolicy> leavePolicies;
}
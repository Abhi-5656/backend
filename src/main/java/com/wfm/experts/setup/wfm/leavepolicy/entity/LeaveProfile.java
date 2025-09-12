package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
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

    @OneToMany(
            mappedBy = "leaveProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<LeaveProfilePolicy> leaveProfilePolicies = new HashSet<>();
}
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "leave_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "leavePolicies")
@ToString(exclude = "leavePolicies")
public class LeaveProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** e.g. "Standard Employee Profile" **/
    @Column(name = "profile_name", nullable = false, unique = true, length = 100)
    private String profileName;

    /**
     * Which leave‐types (policies) are included in this profile.
     * You’ll accept a list of IDs in your DTO, look up each LeavePolicy
     * via its repository, and then call addLeavePolicy(...) before saving.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "leave_profile_leave_policy",
            joinColumns = @JoinColumn(name = "leave_profile_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "leave_policy_id", referencedColumnName = "id")
    )
    @Builder.Default
    private Set<LeavePolicy> leavePolicies = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** convenience methods **/
    public void addLeavePolicy(LeavePolicy policy) {
        this.leavePolicies.add(policy);
        // if you add the inverse side later:
        // policy.getLeaveProfiles().add(this);
    }

    public void removeLeavePolicy(LeavePolicy policy) {
        this.leavePolicies.remove(policy);
        // policy.getLeaveProfiles().remove(this);
    }
}

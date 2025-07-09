package com.wfm.experts.setup.wfm.leavepolicy.entity;// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/ConditionalRule.java


import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.enums.OccurrencePeriod;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Defines a conditional rule that can override base leave policy settings.
 */
@Entity
@Table(name = "conditional_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** maps to `tenure` */
    @Column(name = "tenure")
    private Integer tenure;

    @Column(name = "override_max")
    private Integer overrideMax;

    @Column(name = "override_min_notice")
    private Integer overrideMinNotice;

    @Column(name = "override_min_worked")
    private Integer overrideMinWorked;

    @Column(name = "override_occurrence_limit")
    private Integer overrideOccurrenceLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_occurrence_period", length = 12)
    private OccurrencePeriod overrideOccurrencePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_policy_id", nullable = false)
    private LeavePolicy leavePolicy;

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
}

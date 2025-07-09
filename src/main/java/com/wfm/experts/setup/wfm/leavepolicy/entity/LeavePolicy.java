// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/LeavePolicy.java
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "leave_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "leave_name", nullable = false, length = 100)
    private String leaveName;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_select", nullable = false, length = 20)
    private ProfileSelect profileSelect;

    @Column(name = "calendar_color", length = 20)
    private String calendarColor;

    @Column(name = "enable_leave_config", nullable = false)
    private boolean enableLeaveConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "measure_by", nullable = false, length = 10)
    private MeasureBy measureBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_unpaid", nullable = false, length = 10)
    private PaidUnpaid paidUnpaid;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullDay", column = @Column(name = "full_day", nullable = false)),
            @AttributeOverride(name = "halfDay", column = @Column(name = "half_day", nullable = false))
    })
    private ApplicableFor applicableFor;

    @Column(name = "max_days_year")
    private Integer maxDaysYear;

    @Column(name = "max_days_month")
    private Integer maxDaysMonth;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "min_advance_notice")
    private Integer minAdvanceNotice;

    @Column(name = "min_worked")
    private Integer minWorked;

    @Column(name = "occurrence_limit")
    private Integer occurrenceLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "occurrence_period", nullable = false, length = 12)
    private OccurrencePeriod occurrencePeriod;

    @Column(name = "enable_carryover_proration", nullable = false)
    private boolean enableCarryOverProration;

    @Column(name = "allow_carryover", nullable = false)
    private boolean allowCarryOver;

    @Column(name = "carryover_cap")
    private Integer carryOverCap;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_basis", nullable = false, length = 20)
    private CalculationBasis calculationBasis;

    @Column(name = "auto_encash", nullable = false)
    private boolean autoEncash;

    @Column(name = "allow_proration", nullable = false)
    private boolean allowProration;

    @Enumerated(EnumType.STRING)
    @Column(name = "proration_mode", nullable = false, length = 10)
    private ProrationMode prorationMode;

    @Column(name = "join_date_threshold")
    private Integer joinDateThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding", nullable = false, length = 10)
    private RoundingMode rounding;

    @Column(name = "enable_attachments", nullable = false)
    private boolean enableAttachments;

    @Column(name = "attachment_required", nullable = false)
    private boolean attachmentRequired;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "pdf", column = @Column(name = "pdf", nullable = false)),
            @AttributeOverride(name = "jpg", column = @Column(name = "jpg", nullable = false)),
            @AttributeOverride(name = "png", column = @Column(name = "png", nullable = false)),
            @AttributeOverride(name = "docx", column = @Column(name = "docx", nullable = false))
    })
    private AllowedFileTypes allowedFileTypes;

    @OneToMany(
            mappedBy = "leavePolicy",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ConditionalRule> conditionalRules;

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

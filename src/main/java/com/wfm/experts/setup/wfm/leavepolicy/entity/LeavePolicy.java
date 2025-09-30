package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.Applicability;
import com.wfm.experts.setup.wfm.leavepolicy.enums.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "leave_policy")
@Getter
@Setter
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyName;

    @Column(unique = true)
    private String leaveCode;

//    @Column(nullable = false)
//    private LocalDate effectiveDate;
//
//    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @ElementCollection(targetClass = Applicability.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "leave_policy_applicability", joinColumns = @JoinColumn(name = "policy_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "applicability", nullable = false)
    private List<Applicability> applicableFor;

    @Column(nullable = false)
    private String leaveColor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "calculation_date_config_id", referencedColumnName = "id")
    private CalculationDateConfig calculationDateConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "grants_config_id", referencedColumnName = "id")
    private GrantsConfig grantsConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "limits_config_id", referencedColumnName = "id")
    private LimitsConfig limitsConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attachments_config_id", referencedColumnName = "id")
    private AttachmentsConfig attachmentsConfig;
}
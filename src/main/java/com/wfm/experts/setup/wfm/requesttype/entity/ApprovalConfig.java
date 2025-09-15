package com.wfm.experts.setup.wfm.requesttype.entity;

import com.wfm.experts.setup.wfm.requesttype.enums.ApprovalModeType; // Import the new enum
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "approval_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ApprovalModeType mode; // Replaces 'requires' and 'autoAction'

    @ElementCollection
    @CollectionTable(name = "approval_chain_steps", joinColumns = @JoinColumn(name = "approval_id"))
    @Column(name = "step")
    private List<String> chainSteps;

    @ElementCollection
    @CollectionTable(name = "approval_notify", joinColumns = @JoinColumn(name = "approval_id"))
    @Column(name = "notify_target")
    private List<String> notify;

    // Use Integer wrapper class to allow for null values
    private Integer escalate;

    // Use Integer wrapper class to allow for null values
    private Integer autoDays;
}
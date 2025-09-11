package com.wfm.experts.setup.wfm.requesttype.entity;

import com.wfm.experts.setup.wfm.requesttype.enums.AutoActionType;
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
    private boolean requires;

    @ElementCollection
    @CollectionTable(name = "approval_chain_steps", joinColumns = @JoinColumn(name = "approval_id"))
    @Column(name = "step")
    private List<String> chainSteps;

    @ElementCollection
    @CollectionTable(name = "approval_notify", joinColumns = @JoinColumn(name = "approval_id"))
    @Column(name = "notify_target")
    private List<String> notify;

    private int escalate;

    @Enumerated(EnumType.STRING)
    private AutoActionType autoAction;

    private int autoDays;
}

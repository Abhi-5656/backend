package com.wfm.experts.setup.wfm.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "punch_event_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchEventRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private Integer earlyIn;
    private Integer lateIn;
    private Integer earlyOut;
    private Integer lateOut;
    private boolean notifyOnPunchEvents;
}

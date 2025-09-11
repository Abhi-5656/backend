package com.wfm.experts.setup.wfm.requesttype.entity;

import com.wfm.experts.setup.wfm.requesttype.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "notification_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @ElementCollection(targetClass = NotificationChannel.class)
    @CollectionTable(name = "notification_channels", joinColumns = @JoinColumn(name = "notification_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private List<NotificationChannel> channels;

    // Employee notifications
    private boolean empSubmit;
    private boolean empApprove;
    private boolean empReject;
    private boolean empCancel;

    // Approver notifications
    private boolean approverSubmit;
    private boolean approverEscalate;
    private boolean approverCancel;
    private boolean approverDelete;

    // Listener notifications
    private boolean listenerSubmit;
    private boolean listenerApprove;
    private boolean listenerReject;
    private boolean listenerCancel;
    private boolean listenerDelete;
}

package com.wfm.experts.setup.wfm.requesttype.dto;

import com.wfm.experts.setup.wfm.requesttype.enums.NotificationChannel;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationConfigDTO {

    private Long id;

    private boolean enabled;

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

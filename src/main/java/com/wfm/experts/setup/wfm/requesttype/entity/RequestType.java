// src/main/java/com/wfm/experts/setup/wfm/requesttype/entity/RequestType.java
package com.wfm.experts.setup.wfm.requesttype.entity;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "request_types")
public class RequestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Temporal(TemporalType.DATE)
    private Date effectiveDate;

    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_policy_id")
    private LeavePolicy leavePolicy;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_config_id")
    private ApprovalConfig approval;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "clubbing_config_id")
    private ClubbingConfig clubbing;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_config_id")
    private ValidationConfig validation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_config_id")
    private NotificationConfig notifications;
}
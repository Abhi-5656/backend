package com.wfm.experts.setup.wfm.requesttype.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "request_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The policy name for the request type.
     * It's marked as non-nullable and unique to ensure data integrity.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "approval_id")
    private ApprovalConfig approval;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "clubbing_id")
    private ClubbingConfig clubbing;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "validation_id")
    private ValidationConfig validation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notification_id")
    private NotificationConfig notifications;
}
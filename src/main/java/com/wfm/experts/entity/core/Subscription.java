package com.wfm.experts.entity.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "subscriptions", schema = "public")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String companyName;

    // ✅ Remove @Id if tenantId is not the primary key
    @Column(nullable = false, unique = true, updatable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @JsonIgnore
    private UUID tenantId;

    @Column(nullable = false, unique = true)
    private String adminEmail;

    @Column(nullable = false)
    private String subscriptionType;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false, unique = true, length = 15)
    private String companyGstNumber;

    @Column(nullable = false)
    private Boolean autoRenewal = false;

    @Column(nullable = true)
    private String transactionId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date purchaseDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date activationDate;

    @Column(nullable = true)
    private String status;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)  // Updated to non-nullable
    private String companyDomain;  // Renamed field for storing company email domain

    @Column(nullable = false)
    @JsonIgnore
    private String tenantSchema;

    // ✅ JPA Will Automatically Save Modules When Saving Subscription
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "subscription_id")
    private List<SubscriptionModule> modules;

    @PrePersist
    protected void onCreate() {
        this.purchaseDate = (this.purchaseDate != null) ? this.purchaseDate : new Date();
        this.activationDate = (this.activationDate != null) ? this.activationDate : new Date();
        this.transactionId = (this.transactionId != null) ? this.transactionId.replace("-", "") : "TXN-" + System.currentTimeMillis();
        this.status = (this.status != null) ? this.status : "ACTIVE";
    }
}

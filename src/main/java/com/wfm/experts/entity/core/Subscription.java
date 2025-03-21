package com.wfm.experts.entity.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "subscriptions", schema = "public")  // Subscription table in public schema
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false, unique = true)
    private String companyName;  // The company name (Used to derive `tenantId`)

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    @JsonIgnore
    private String tenantId;  // 🔹 Derived from `companyName` (used for path-based multi-tenancy)

    @Column(nullable = false, unique = true)
    private String adminEmail;  // Email of the admin user of the tenant

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

    @Column(nullable = false)
    @JsonIgnore
    private String tenantSchema;  // 🔹 Database schema corresponding to this tenant

    @PrePersist
    protected void onCreate() {
        this.purchaseDate = (this.purchaseDate != null) ? this.purchaseDate : new Date();
        this.activationDate = (this.activationDate != null) ? this.activationDate : new Date();
        this.transactionId = (this.transactionId != null) ? this.transactionId.replace("-", "") : "TXN-" + System.currentTimeMillis();
        this.status = (this.status != null) ? this.status : "ACTIVE";
    }
}

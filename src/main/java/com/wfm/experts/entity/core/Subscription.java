/*
 *
 *  * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 *  *
 *  * This software, including all associated files, documentation, and related materials,
 *  * is the proprietary property of WFM EXPERTS INDIA PVT LTD. Unauthorized copying,
 *  * distribution, modification, or any form of use beyond the granted permissions
 *  * without prior written consent is strictly prohibited.
 *  *
 *  * DISCLAIMER:
 *  * This software is provided "as is," without warranty of any kind, express or implied,
 *  * including but not limited to the warranties of merchantability, fitness for a particular
 *  * purpose, and non-infringement.
 *  *
 *  * For inquiries, contact legal@wfmexperts.com.
 *
 */

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

    @Column(nullable = false, unique = true)
    private String tenantURL;  // 🔹 URL for tenant-specific access

    @PrePersist
    protected void onCreate() {
        this.purchaseDate = (this.purchaseDate != null) ? this.purchaseDate : new Date();
        this.activationDate = (this.activationDate != null) ? this.activationDate : new Date();
        this.transactionId = (this.transactionId != null) ? this.transactionId.replace("-", "") : "TXN-" + System.currentTimeMillis();
        this.status = (this.status != null) ? this.status : "ACTIVE";
    }
}

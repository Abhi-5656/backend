package com.wfm.experts.tenant.common.setup.wizard.entity;


import com.wfm.experts.tenant.common.core.Subscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
// This MUST be in the 'public' schema to manage the pre-tenant creation process.
@Table(name = "setup_wizards")

public class SetupWizard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Data from "Organization" Screen ---
    @Lob
    private String companyLogo;
    private String companyName;
    private String companySize;
    private String industry;
    private String companyAddress;
    private String complianceRegion;

    // --- Data from "Welcome" Screen (Modules already purchased) ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "setup_wizard_modules", joinColumns = @JoinColumn(name = "wizard_id"), schema = "public")
    @Column(name = "module_name")
    private List<String> purchasedModules;

    // --- Wizard Status & Link to Final Subscription ---
    @Column(nullable = false)
    private String status = "IN_PROGRESS";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}

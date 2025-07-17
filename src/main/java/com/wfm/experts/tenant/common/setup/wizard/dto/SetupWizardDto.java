package com.wfm.experts.tenant.common.setup.wizard.dto;

import lombok.Data;
import java.util.List;

@Data
public class SetupWizardDto {

    private Long id;

    // --- Organization Details ---
    private String companyLogo;
    private String companyName;
    private String companySize;
    private String industry;
    private String companyAddress;
    private String complianceRegion;

    // --- Module Details ---
    private List<String> purchasedModules;

    // --- Wizard Status ---
    private String status;
}
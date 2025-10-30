package com.wfm.experts.tenant.common.setup.wizard.service;

import com.wfm.experts.tenant.common.setup.wizard.dto.SetupWizardDto;
import com.wfm.experts.tenant.common.subscription.dto.SubscriptionDTO;

public interface SetupWizardService {

    SetupWizardDto startWizard(SetupWizardDto initialDto);

    SetupWizardDto saveOrganizationDetails(Long wizardId, SetupWizardDto detailsDto);

    // Changed return type: now DTO
    SubscriptionDTO finishWizard(Long wizardId, SetupWizardDto adminDetailsDto) throws Exception;
}

package com.wfm.experts.tenant.common.setup.wizard.service;

import com.wfm.experts.tenant.common.setup.wizard.dto.SetupWizardDto;
import com.wfm.experts.tenant.common.subscription.entity.Subscription;

/**
 * Service interface for managing the multi-step setup wizard process.
 * This service orchestrates the creation of a SetupWizard entity, updates it
 * as the user progresses, and finally triggers the creation of a new tenant
 * and subscription upon completion.
 */
public interface SetupWizardService {

    /**
     * Starts a new setup wizard process. This is the first step.
     *
     * @param initialDto A DTO containing initial data, like the modules purchased.
     * @return The created SetupWizardDto with its generated ID.
     */
    SetupWizardDto startWizard(SetupWizardDto initialDto);

    /**
     * Saves the organization details from the second step of the wizard.
     *
     * @param wizardId The ID of the ongoing wizard.
     * @param detailsDto A DTO containing the company's details (name, logo, etc.).
     * @return The updated SetupWizardDto.
     */
    SetupWizardDto saveOrganizationDetails(Long wizardId, SetupWizardDto detailsDto);

    /**
     * Finalizes the setup wizard. This method takes all the collected information
     * from the SetupWizard entity and uses it to create the actual Subscription
     * and the new tenant environment.
     *
     * @param wizardId The ID of the wizard to finalize.
     * @param adminDetailsDto A DTO containing the details for the initial admin user.
     * @return The newly created Subscription entity.
     * @throws Exception if any part of the tenant or admin creation fails.
     */
    Subscription finishWizard(Long wizardId, SetupWizardDto adminDetailsDto) throws Exception;

}
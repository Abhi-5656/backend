package com.wfm.experts.tenant.common.setup.wizard.service.impl;

import com.wfm.experts.tenant.common.core.Subscription;
import com.wfm.experts.tenant.common.core.SubscriptionModule;
import com.wfm.experts.service.SubscriptionService;
import com.wfm.experts.tenant.common.setup.wizard.dto.SetupWizardDto;
import com.wfm.experts.tenant.common.setup.wizard.entity.SetupWizard;
import com.wfm.experts.tenant.common.setup.wizard.mapper.SetupWizardMapper;
import com.wfm.experts.tenant.common.setup.wizard.repository.SetupWizardRepository;
import com.wfm.experts.tenant.common.setup.wizard.service.SetupWizardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the SetupWizardService.
 * This class orchestrates the multi-step tenant setup process.
 */
@Service
public class SetupWizardServiceImpl implements SetupWizardService {

    @Autowired
    private SetupWizardRepository setupWizardRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SetupWizardMapper setupWizardMapper;

    /**
     * Starts a new setup wizard process by creating an initial record.
     */
    @Override
    @Transactional
    public SetupWizardDto startWizard(SetupWizardDto initialDto) {
        SetupWizard wizard = new SetupWizard();
        wizard.setPurchasedModules(initialDto.getPurchasedModules());
        SetupWizard savedWizard = setupWizardRepository.save(wizard);
        return setupWizardMapper.toDto(savedWizard);
    }

    /**
     * Saves the organization details provided in the second step of the wizard.
     */
    @Override
    @Transactional
    public SetupWizardDto saveOrganizationDetails(Long wizardId, SetupWizardDto detailsDto) {
        SetupWizard wizard = setupWizardRepository.findById(wizardId)
                .orElseThrow(() -> new RuntimeException("SetupWizard not found with ID: " + wizardId));

        // Update the wizard entity with details from the DTO
        wizard.setCompanyLogo(detailsDto.getCompanyLogo());
        wizard.setCompanyName(detailsDto.getCompanyName());
        wizard.setCompanySize(detailsDto.getCompanySize());
        wizard.setIndustry(detailsDto.getIndustry());
        wizard.setCompanyAddress(detailsDto.getCompanyAddress());
        wizard.setComplianceRegion(detailsDto.getComplianceRegion());

        SetupWizard updatedWizard = setupWizardRepository.save(wizard);
        return setupWizardMapper.toDto(updatedWizard);
    }

    /**
     * Finalizes the wizard, creates the tenant, and links the subscription.
     */
    @Override
    @Transactional
    public Subscription finishWizard(Long wizardId, SetupWizardDto adminDetailsDto) throws Exception {
        SetupWizard wizard = setupWizardRepository.findById(wizardId)
                .orElseThrow(() -> new RuntimeException("SetupWizard not found with ID: " + wizardId));

        // 1. Construct the final Subscription object from the wizard data
        Subscription subscription = new Subscription();
        subscription.setCompanyName(wizard.getCompanyName());
        // Set other necessary subscription fields from the wizard entity
        // For example, if you add GST number or other fields to the wizard:
        // subscription.setCompanyGstNumber(wizard.getCompanyGstNumber());

        // Convert module names from the wizard into SubscriptionModule entities
        List<SubscriptionModule> modules = wizard.getPurchasedModules().stream()
                .map(moduleName -> {
                    SubscriptionModule module = new SubscriptionModule();
                    module.setModuleName(moduleName);
                    return module;
                })
                .collect(Collectors.toList());
        subscription.setModules(modules);

        // 2. Call the existing SubscriptionService to perform the core tenant creation logic
        Subscription createdSubscription = subscriptionService.createSubscription(
                subscription,
                adminDetailsDto.getCompanyName(), // Assuming admin details are passed in the DTO for now
                adminDetailsDto.getCompanySize(),   // Adjust as per your final DTO structure for admin details
                adminDetailsDto.getIndustry(),      // adminEmail
                adminDetailsDto.getCompanyAddress(),// employeeId
                adminDetailsDto.getComplianceRegion() // phoneNumber
        );

        // 3. Update the wizard's status to 'COMPLETED' and link it to the new subscription
        wizard.setStatus("COMPLETED");
        wizard.setSubscription(createdSubscription);
        setupWizardRepository.save(wizard);

        return createdSubscription;
    }
}
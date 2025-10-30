package com.wfm.experts.tenant.common.setup.wizard.service.impl;

import com.wfm.experts.tenant.common.subscription.dto.SubscriptionDTO;
import com.wfm.experts.tenant.common.subscription.dto.SubscriptionModuleDTO;
import com.wfm.experts.tenant.common.subscription.entity.Subscription;
import com.wfm.experts.tenant.common.subscription.repository.SubscriptionRepository; // <-- add
import com.wfm.experts.tenant.common.subscription.service.SubscriptionService;
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

    // NEW: to link wizard -> Subscription entity without changing DB model
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Starts a new setup wizard process by creating an initial record.
     */
    @Override
    @Transactional
    public SetupWizardDto startWizard(SetupWizardDto initialDto) {
        var wizard = new SetupWizard();
        wizard.setPurchasedModules(initialDto.getPurchasedModules());
        var savedWizard = setupWizardRepository.save(wizard);
        return setupWizardMapper.toDto(savedWizard);
    }

    /**
     * Saves the organization details provided in the second step of the wizard.
     */
    @Override
    @Transactional
    public SetupWizardDto saveOrganizationDetails(Long wizardId, SetupWizardDto detailsDto) {
        var wizard = setupWizardRepository.findById(wizardId)
                .orElseThrow(() -> new RuntimeException("SetupWizard not found with ID: " + wizardId));

        // Update the wizard entity with details from the DTO
        wizard.setCompanyLogo(detailsDto.getCompanyLogo());
        wizard.setCompanyName(detailsDto.getCompanyName());
        wizard.setCompanySize(detailsDto.getCompanySize());
        wizard.setIndustry(detailsDto.getIndustry());
        wizard.setCompanyAddress(detailsDto.getCompanyAddress());
        wizard.setComplianceRegion(detailsDto.getComplianceRegion());

        var updatedWizard = setupWizardRepository.save(wizard);
        return setupWizardMapper.toDto(updatedWizard);
    }

    /**
     * Finalizes the wizard, creates the tenant, and links the subscription.
     */
    @Override
    @Transactional
    public SubscriptionDTO finishWizard(Long wizardId, SetupWizardDto adminDetailsDto) throws Exception {
        var wizard = setupWizardRepository.findById(wizardId)
                .orElseThrow(() -> new RuntimeException("SetupWizard not found with ID: " + wizardId));

        // 1) Build SubscriptionDTO from wizard data (same fields as before)
        var subscription = new SubscriptionDTO();
        subscription.setCompanyName(wizard.getCompanyName());
        // If you later add more fields (e.g., GST), set them here
        // subscription.setCompanyGstNumber(wizard.getCompanyGstNumber());

        // Modules -> DTOs
        List<SubscriptionModuleDTO> modules = wizard.getPurchasedModules().stream()
                .map(name -> SubscriptionModuleDTO.builder().moduleName(name).build())
                .collect(Collectors.toList());
        subscription.setModules(modules);

        // 2) Create subscription using DTO service
        SubscriptionDTO createdSubscription = subscriptionService.createSubscription(
                subscription,
                adminDetailsDto.getCompanyName(),     // firstName (as per your previous placeholder mapping)
                adminDetailsDto.getCompanySize(),     // lastName
                adminDetailsDto.getIndustry(),        // email
                adminDetailsDto.getCompanyAddress(),  // employeeId
                adminDetailsDto.getComplianceRegion() // phoneNumber
        );

        // 3) Update wizard status and link to actual Subscription entity (unchanged DB relation)
        wizard.setStatus("COMPLETED");

        // Link to the persisted Subscription entity using the id from DTO
        if (createdSubscription != null && createdSubscription.getId() != null) {
            Subscription subscriptionEntity = subscriptionRepository
                    .findById(createdSubscription.getId())
                    .orElse(null);
            if (subscriptionEntity != null) {
                wizard.setSubscription(subscriptionEntity);
            }
        }

        setupWizardRepository.save(wizard);

        // Return DTO to caller (controller/UI)
        return createdSubscription;
    }
}

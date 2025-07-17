package com.wfm.experts.controller;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.tenant.common.setup.wizard.dto.SetupWizardDto;
import com.wfm.experts.tenant.common.setup.wizard.service.SetupWizardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller to manage the multi-step tenant setup wizard.
 * This controller is responsible for guiding the user through the setup process,
 * from module selection to the final creation of the tenant.
 */
@RestController
@RequestMapping("/api/setup-wizard")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Allows cross-origin requests, useful for development
public class SetupWizardController {

    @Autowired
    private SetupWizardService setupWizardService;

    /**
     * Step 1: Starts the wizard process.
     * The frontend sends the list of purchased modules to create an initial wizard record.
     *
     * @param initialDto A DTO containing the list of purchased modules.
     * @return The initial state of the wizard, including its unique ID.
     */
    @PostMapping("/start")
    public ResponseEntity<SetupWizardDto> startWizard(@RequestBody SetupWizardDto initialDto) {
        SetupWizardDto wizard = setupWizardService.startWizard(initialDto);
        return ResponseEntity.ok(wizard);
    }

    /**
     * Step 2: Saves the organization's details.
     * The frontend sends the company information collected from the "Organization" screen.
     *
     * @param wizardId The ID of the ongoing wizard, returned from the /start endpoint.
     * @param detailsDto A DTO containing the organization's details.
     * @return The updated state of the wizard.
     */
    @PostMapping("/{wizardId}/organization")
    public ResponseEntity<SetupWizardDto> saveOrganizationDetails(
            @PathVariable Long wizardId,
            @RequestBody SetupWizardDto detailsDto) {
        SetupWizardDto wizard = setupWizardService.saveOrganizationDetails(wizardId, detailsDto);
        return ResponseEntity.ok(wizard);
    }

    /**
     * Step 4: Finishes the wizard and creates the tenant.
     * The frontend sends the details for the initial administrator account.
     *
     * @param wizardId The ID of the wizard to be finalized.
     * @param adminDetailsDto A DTO containing the admin user's details.
     * @return The newly created Subscription object, confirming the successful setup.
     */
    @PostMapping("/{wizardId}/finish")
    public ResponseEntity<Subscription> finishWizard(
            @PathVariable Long wizardId,
            @RequestBody SetupWizardDto adminDetailsDto) {
        try {
            Subscription subscription = setupWizardService.finishWizard(wizardId, adminDetailsDto);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            // In a real application, you would have more specific error handling here
            return ResponseEntity.status(500).body(null);
        }
    }
}
package com.wfm.experts.tenant.common.subscription.controller;

import com.wfm.experts.tenant.common.subscription.dto.SubscriptionDTO;
import com.wfm.experts.tenant.common.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Create a Subscription with Admin Details and Modules (DTO in/out)
    @PostMapping("/create")
    public ResponseEntity<SubscriptionDTO> createSubscription(
            @RequestBody @Valid SubscriptionDTO subscriptionDto,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String employeeId,
            @RequestParam String phoneNumber) throws Exception {

        SubscriptionDTO created = subscriptionService.createSubscription(
                subscriptionDto, firstName, lastName, email, employeeId, phoneNumber);

        return ResponseEntity.ok(created);
    }
}

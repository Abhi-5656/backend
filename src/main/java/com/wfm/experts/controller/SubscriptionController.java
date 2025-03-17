package com.wfm.experts.controller;

import com.wfm.experts.entity.core.Subscription;
import com.wfm.experts.service.SubscriptionService;
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

    // ✅ Create a Subscription with Admin Details and Modules
    @PostMapping("/create")
    public ResponseEntity<Subscription> createSubscription(
            @RequestBody Subscription subscription,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String employeeId,
            @RequestParam String phoneNumber) throws Exception {

        Subscription createdSubscription = subscriptionService.createSubscription(
                subscription, firstName, lastName, email, employeeId, phoneNumber);

        return ResponseEntity.ok(createdSubscription);
    }

    // ✅ Get Subscription by ID
    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    // ✅ Get All Subscriptions
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

}

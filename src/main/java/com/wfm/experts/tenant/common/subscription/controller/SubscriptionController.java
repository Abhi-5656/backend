package com.wfm.experts.tenant.common.subscription.controller;/*
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


import com.wfm.experts.tenant.common.subscription.entity.Subscription;
import com.wfm.experts.tenant.common.subscription.service.SubscriptionService;
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

    //Create a Subscription with Admin Details and Modules
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


}

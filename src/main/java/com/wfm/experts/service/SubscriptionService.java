package com.wfm.experts.service;

import com.wfm.experts.tenant.common.core.Subscription;

public interface SubscriptionService {

    /**
     * ✅ Creates a new subscription and stores the purchaser's details.
     *
     * @param subscription The subscription details.
     * @param firstName Purchaser's first name.
     * @param lastName Purchaser's last name.
     * @param email Purchaser's email address.
     * @param employeeId Unique employee identifier.
     * @param phoneNumber Purchaser's contact number.
     * @return The created subscription object.
     * @throws Exception If any error occurs during subscription creation.
     */
    Subscription createSubscription(
            Subscription subscription,
            String firstName,
            String lastName,
            String email,
            String employeeId,
            String phoneNumber
    ) throws Exception;


}

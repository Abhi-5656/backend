package com.wfm.experts.service;

import com.wfm.experts.entity.core.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * ✅ Retrieves a subscription by ID.
     */
    Subscription getSubscriptionById(Long id);

    /**
     * ✅ Retrieves all subscriptions.
     */
    List<Subscription> getAllSubscriptions();

    // ✅ Find Subscription by Tenant ID (Use UUID)
//    Optional<Subscription> findByTenantId(UUID tenantId);


}

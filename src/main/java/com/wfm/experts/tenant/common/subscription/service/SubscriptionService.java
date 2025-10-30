package com.wfm.experts.tenant.common.subscription.service;

import com.wfm.experts.tenant.common.subscription.dto.SubscriptionDTO;

public interface SubscriptionService {

    SubscriptionDTO createSubscription(
            SubscriptionDTO subscriptionDto,
            String firstName,
            String lastName,
            String email,
            String employeeId,
            String phoneNumber) throws Exception;
}

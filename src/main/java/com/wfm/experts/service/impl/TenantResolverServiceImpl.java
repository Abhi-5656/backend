package com.wfm.experts.service.impl;

import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.service.TenantResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * ✅ Implementation of `TenantResolverService`.
 * ✅ Resolves Tenant ID (`tenant_id`) dynamically based on email.
 */
@Service
public class TenantResolverServiceImpl implements TenantResolverService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public TenantResolverServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * ✅ Resolves the `tenant_id` for a given email.
     * ✅ Fetches it from the `subscriptions` table.
     */
    @Override
    public UUID resolveTenantId(String email) {
        Optional<UUID> tenantIdOpt = subscriptionRepository.findTenantIdByAdminEmail(email);
        return tenantIdOpt.orElse(null);  // 🔹 Return `null` if no matching tenant found
    }
}

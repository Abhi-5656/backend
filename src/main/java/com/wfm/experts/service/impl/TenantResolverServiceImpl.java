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
     * ✅ Fetches it from the `subscriptions` table based on `companyDomain` (extracted from email).
     */
    @Override
    public UUID resolveTenantId(String email) {
        String emailDomain = extractEmailDomain(email);  // Extract domain part from email

        System.out.println("Email domain: " + emailDomain);

        // Find tenant ID based on the company domain
        Optional<UUID> tenantIdOpt = subscriptionRepository.findByCompanyDomain(emailDomain);

        System.out.println("Tenant ID: " + tenantIdOpt.orElse(null));

        return tenantIdOpt.orElse(null);  // 🔹 Return `null` if no matching tenant found
    }

    /**
     * ✅ Helper method to extract email domain (the part after @)
     */
    private String extractEmailDomain(String email) {
        if (email != null && email.contains("@")) {
            return email.split("@")[1].toLowerCase();  // Extract the part after '@' (e.g., "wfmexperts.com")
        }
        throw new IllegalArgumentException("Invalid email format: " + email);
    }
}

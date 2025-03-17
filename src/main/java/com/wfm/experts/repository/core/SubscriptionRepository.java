package com.wfm.experts.repository.core;

import com.wfm.experts.entity.core.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // ✅ Find Subscription by ID
    Optional<Subscription> findById(Long id);

    // ✅ Find Subscription by Tenant ID (UUID)
    Optional<Subscription> findByTenantId(UUID tenantId);  // ✅ Use UUID instead of String



    // ✅ Find All Subscriptions by Company Name
    List<Subscription> findByCompanyName(String companyName);

    // ✅ Find All Active Subscriptions
    List<Subscription> findByIsActiveTrue();

    // ✅ Find Subscriptions by Type (CLIENT or MODULE)
    List<Subscription> findByEntityType(String entityType);
}

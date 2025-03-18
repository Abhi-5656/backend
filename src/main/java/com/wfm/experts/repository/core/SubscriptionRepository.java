package com.wfm.experts.repository.core;

import com.wfm.experts.entity.core.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // ✅ Find Subscription by ID
    Optional<Subscription> findById(Long id);

    /**
     * ✅ Fetches `tenant_id` by admin email.
     */
    @Query("SELECT s.tenantId FROM Subscription s WHERE s.adminEmail = :email")
    Optional<UUID> findTenantIdByAdminEmail(String email);

    /**
     * ✅ Find the `tenantId` by `companyDomain`.
     * This query selects only the tenantId from the subscriptions table.
     */
    @Query("SELECT s.tenantId FROM Subscription s WHERE s.companyDomain = :companyDomain")
    Optional<UUID> findByCompanyDomain(String companyDomain);  // Query to fetch only the tenantId
}

package com.wfm.experts.repository.core;

import com.wfm.experts.tenant.common.core.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    // ✅ Check if a tenant exists by `tenantId`
    boolean existsByTenantId(String tenantId);

    // ✅ Find a tenant by `tenantId`
    Optional<Subscription> findByTenantId(String tenantId);


    /**
     * ✅ Finds the Tenant ID based on the provided schema name.
     */
    Optional<String> findTenantIdByTenantSchema(String tenantSchema);


}

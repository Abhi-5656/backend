package com.wfm.experts.util;

import com.wfm.experts.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ✅ Utility class for managing multi-tenant schemas dynamically.
 */
@Component
public class TenantSchemaUtil {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ✅ Switches to the correct schema based on Tenant ID.
     */
    @Transactional
    public void switchToTenantSchema() {
        UUID tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            throw new RuntimeException("❌ Tenant ID is missing from context. Cannot switch schema!");
        }

        try {
            String schemaName = (String) entityManager.createQuery(
                            "SELECT s.tenantSchema FROM Subscription s WHERE s.tenantId = :tenantId")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();

            if (schemaName == null || schemaName.isBlank()) {
                throw new RuntimeException("❌ Tenant schema is empty. Check subscription data.");
            }

            // ✅ Switch schema
            entityManager.createNativeQuery("SET search_path TO " + schemaName).executeUpdate();
            System.out.println("✅ Switched to Tenant Schema: " + schemaName);
        } catch (NoResultException e) {
            throw new RuntimeException("❌ Tenant ID `" + tenantId + "` not found in subscriptions table!");
        }
    }
}

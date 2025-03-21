package com.wfm.experts.util;

import com.wfm.experts.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.logging.Logger;

/**
 * ✅ Utility for dynamically switching schemas in a multi-tenant database.
 */

@Component
public class TenantSchemaUtil {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOGGER = Logger.getLogger(TenantSchemaUtil.class.getName());

    /**
     * Automatically switches to the correct schema based on the tenantId stored in TenantContext.
     */
    @Transactional
    public void ensureTenantSchemaIsSet() {
        // Retrieve the tenantId from the TenantContext
        String tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            LOGGER.warning("Tenant ID not set in context. Skipping schema switch.");
            return;  // Skip schema switching if no tenant context is set
        }

        try {
            // Fetch tenant schema from the subscription table using tenantId
            String schemaName = (String) entityManager.createQuery(
                            "SELECT s.tenantSchema FROM Subscription s WHERE s.tenantId = :tenantId")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();

            if (schemaName == null || schemaName.isBlank()) {
                throw new RuntimeException("Tenant schema is empty. Check subscription data.");
            }

            // Set schema dynamically for multi-tenancy
            entityManager.createNativeQuery("SET search_path TO " + schemaName).executeUpdate();

            LOGGER.info("✅ Switched to Tenant Schema: " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Error switching to tenant schema: " + e.getMessage());
        }
    }
}


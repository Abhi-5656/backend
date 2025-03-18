package com.wfm.experts.util;

import com.wfm.experts.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Utility class for managing multi-tenant schema switching dynamically.
 * Ensures database queries run under the correct tenant schema.
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
    public void switchToTenantSchema() {
        // Retrieve the tenantId from the TenantContext (set by JwtAuthenticationFilter)
        UUID tenantId = TenantContext.getTenant();

        // Handle the case where Tenant ID is missing in the context (public endpoints or error scenarios)
        if (tenantId == null) {
            LOGGER.warning("Tenant ID not set in context. Skipping schema switch.");

            // If this is a public endpoint, don't need to throw an exception
            // Just skip the schema switch logic
            return;  // Skip schema switching
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

            // Store tenant in context for thread safety (already set by JwtAuthenticationFilter, but ensuring it here)
            TenantContext.setTenant(tenantId);

            LOGGER.info("✅ Switched to Tenant Schema: " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Error switching to tenant schema: " + e.getMessage());
        }
    }
}

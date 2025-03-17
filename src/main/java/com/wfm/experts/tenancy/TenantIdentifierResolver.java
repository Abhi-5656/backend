package com.wfm.experts.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ✅ Resolves the current tenant ID (UUID) for Hibernate multi-tenancy.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        UUID tenantId = TenantContext.getTenant(); // ✅ Get UUID from TenantContext
        return (tenantId != null) ? tenantId.toString() : "public"; // ✅ Convert UUID to string if exists
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

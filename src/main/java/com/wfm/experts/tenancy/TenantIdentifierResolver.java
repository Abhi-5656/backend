package com.wfm.experts.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * ✅ Resolves the correct tenant ID for Hibernate multi-tenancy.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenant();
        return (tenantId != null) ? tenantId : "public"; // ✅ Default to public if tenant is missing
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

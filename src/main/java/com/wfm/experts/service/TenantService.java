package com.wfm.experts.service;

import java.util.Map;

/**
 * Interface for managing tenant schemas and applying migrations.
 */
import java.util.Map;
import java.util.UUID;

public interface TenantService {

    /**
     * ✅ Creates a new schema for the tenant, generates a unique `tenantId`,
     * and applies Flyway migrations.
     *
     * @param companyName The company name (used to generate schema).
     * @return A map containing `tenantId` (UUID) and `tenantUrl`.
     * @throws Exception if schema creation or migration fails.
     */
    Map<String, Object> createTenantSchema(String companyName) throws Exception;
}

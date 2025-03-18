package com.wfm.experts.service;

import java.util.UUID;

/**
 * ✅ Interface for resolving Tenant ID (`tenant_id`) from user email.
 * ✅ Helps in multi-tenant authentication without needing `X-Tenant-ID`.
 */
public interface TenantResolverService {

    /**
     * ✅ Resolves the `tenant_id` for a given email.
     *
     * @param email The user's email.
     * @return The `tenant_id` as a UUID, or `null` if not found.
     */
    UUID resolveTenantId(String email);
}

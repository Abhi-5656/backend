package com.wfm.experts.tenancy;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ✅ Manages the current tenant context using ThreadLocal storage.
 */
@Component
public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static void setTenant(UUID tenant) {
        currentTenant.set(tenant);
    }

    public static UUID getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

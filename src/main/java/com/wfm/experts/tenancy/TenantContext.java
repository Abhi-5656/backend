package com.wfm.experts.tenancy;

import org.springframework.stereotype.Component;

/**
 * ✅ Manages the current tenant context using ThreadLocal storage.
 */
@Component
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

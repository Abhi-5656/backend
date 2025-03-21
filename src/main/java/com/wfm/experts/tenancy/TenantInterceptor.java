package com.wfm.experts.tenancy;

import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.util.TenantSchemaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.logging.Logger;

/**
 * ✅ Intercepts requests to determine the correct tenant schema.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = Logger.getLogger(TenantInterceptor.class.getName());

    private final TenantSchemaUtil tenantSchemaUtil;
    private final SubscriptionRepository subscriptionRepository;

    public TenantInterceptor(TenantSchemaUtil tenantSchemaUtil, SubscriptionRepository subscriptionRepository) {
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = TenantContext.getTenant();  // Retrieve tenant from context

        if (tenantId == null) {
            LOGGER.severe("❌ Tenant ID is missing in request context.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant ID is missing.");
            return false;
        }

        // Validate if tenant exists
        boolean tenantExists = subscriptionRepository.existsByTenantId(tenantId);
        if (!tenantExists) {
            LOGGER.severe("❌ Tenant not found for tenant ID: " + tenantId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant not found.");
            return false;
        }

        // Set the tenant context and switch the schema
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.ensureTenantSchemaIsSet();  // Switch to the tenant schema

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();  // Clear tenant context after request completes
    }
}



package com.wfm.experts.tenancy;

import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.util.TenantSchemaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * ✅ Interceptor to switch the tenant schema dynamically before processing requests.
 * ✅ Supports extracting tenant ID from JWT Token if the X-Tenant-Id header is missing.
 * ✅ Skips schema switching for authentication & subscription APIs.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = Logger.getLogger(TenantInterceptor.class.getName());

    @Autowired
    private TenantSchemaUtil tenantSchemaUtil;

    @Autowired
    private JwtUtil jwtUtil; // ✅ Utility to extract claims from JWT tokens

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();  // ✅ Get the requested URL

        // ✅ Skip schema switching for authentication & subscription requests
        if (isPublicRequest(requestUri)) {
            LOGGER.info("🔓 Public request detected: " + requestUri);
            return true; // ✅ Continue without schema switching
        }

        // ✅ Extract tenant ID from JWT or Header
        UUID tenantId = extractTenantId(request);
        if (tenantId == null) {
            throw new RuntimeException("❌ Missing Tenant ID! Please provide a valid JWT token or X-Tenant-Id header.");
        }

        // ✅ Set Tenant ID in Context & Switch Schema
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.switchToTenantSchema();
        LOGGER.info("✅ Tenant schema switched for: " + tenantId);

        return true; // ✅ Continue request processing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear(); // ✅ Clean up after request completes
    }

    /**
     * ✅ Extracts Tenant ID from JWT Token or Header.
     */
    private UUID extractTenantId(HttpServletRequest request) {
        String tenantIdHeader = request.getHeader("X-Tenant-Id");

        // ✅ 1. Try extracting tenant ID from the Header (if exists)
        if (tenantIdHeader != null && !tenantIdHeader.trim().isEmpty()) {
            try {
                return UUID.fromString(tenantIdHeader.trim()); // ✅ Ensure valid UUID format
            } catch (IllegalArgumentException e) {
                LOGGER.severe("❌ Invalid UUID format for X-Tenant-Id: " + tenantIdHeader);
                return null;
            }
        }

        // ✅ 2. If Header is missing, extract from JWT Token
        String jwtToken = extractJwtFromRequest(request);
        if (jwtToken != null) {
            try {
                return jwtUtil.extractTenantId(jwtToken); // ✅ Extract "tenantId" as UUID directly
            } catch (Exception e) {
                LOGGER.severe("❌ Error extracting Tenant ID from JWT Token: " + e.getMessage());
                return null;
            }
        }

        return null; // 🔴 No Tenant ID found
    }

    /**
     * ✅ Extracts the JWT token from the Authorization Header.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // ✅ Remove "Bearer " prefix
        }
        return null;
    }

    /**
     * ✅ Determines if a request should be treated as public.
     * Add all public routes that don't require Tenant ID here.
     */
    private boolean isPublicRequest(String requestUri) {
        return requestUri.startsWith("/api/auth") || requestUri.startsWith("/api/subscriptions");
    }
}

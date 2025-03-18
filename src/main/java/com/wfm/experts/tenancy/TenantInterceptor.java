package com.wfm.experts.tenancy;

import com.wfm.experts.util.TenantSchemaUtil;
import com.wfm.experts.security.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Intercepts requests to switch schema based on tenant ID.
 * It will extract the tenantId from JWT and set it in the context for schema switching.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = Logger.getLogger(TenantInterceptor.class.getName());

    private final TenantSchemaUtil tenantSchemaUtil;
    private final JwtUtil jwtUtil;

    public TenantInterceptor(TenantSchemaUtil tenantSchemaUtil, JwtUtil jwtUtil) {
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Intercepts each request to either switch schema or skip it for public endpoints.
     * @param request The incoming HttpServletRequest.
     * @param response The outgoing HttpServletResponse.
     * @param handler The handler to execute.
     * @return boolean Whether to continue processing the request.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI(); // Get the requested URL

        // Skip schema switching for public endpoints
        if (isPublicRequest(requestUri)) {
            LOGGER.info("Skipping tenant schema switch for public request: " + requestUri);
            return true; // Continue without schema switching
        }

        // Extract tenantId from JWT Token
        UUID tenantId = extractTenantId(request);
        if (tenantId == null) {
            throw new RuntimeException("Missing Tenant ID! Please provide a valid JWT token.");
        }

        // Set the Tenant ID in the TenantContext for schema switching
        TenantContext.setTenant(tenantId);

        // Switch the schema based on the extracted tenantId
        tenantSchemaUtil.switchToTenantSchema();

        LOGGER.info("Switched to tenant schema for tenantId: " + tenantId);

        return true; // Continue with request processing
    }

    /**
     * Extracts Tenant ID from JWT token or the request's Authorization header.
     * @param request The HttpServletRequest.
     * @return UUID The tenantId extracted from the JWT token.
     */
    private UUID extractTenantId(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null; // No token found, cannot extract tenant ID
        }

        // Extract token from the "Bearer <JWT>" format
        String token = authorizationHeader.substring(7);

        try {
            // Extract tenant ID from the JWT token
            return jwtUtil.extractTenantId(token);
        } catch (Exception e) {
            LOGGER.severe("Failed to extract tenantId from JWT token: " + e.getMessage());
            return null; // Return null if extraction fails
        }
    }

    /**
     * Determines if the current request is a public request that should bypass tenant schema switching.
     * @param requestUri The requested URI path.
     * @return boolean True if the request is public and should bypass schema switching, false otherwise.
     */
    private boolean isPublicRequest(String requestUri) {
        // Define public endpoints that should not require tenant schema switching
        return requestUri.startsWith("/api/auth") || requestUri.startsWith("/api/subscriptions");
    }

    /**
     * After request is processed, clear the tenant context.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear(); // Clear the tenant context to avoid potential leaks
    }
}

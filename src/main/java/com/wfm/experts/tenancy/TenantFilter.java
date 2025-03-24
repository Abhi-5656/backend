package com.wfm.experts.tenancy;

import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(TenantFilter.class.getName());
    private static final String PUBLIC_API_PREFIX = "/api/subscriptions"; // Public API for subscriptions
    private static final String LOGIN_API_PREFIX = "/api/auth/login"; // Exception case for login endpoint

    private final SubscriptionRepository subscriptionRepository;
    private final JwtUtil jwtUtil;  // Inject JwtUtil to extract claims from JWT token

    // Injecting SubscriptionRepository and JwtUtil directly
    public TenantFilter(SubscriptionRepository subscriptionRepository, JwtUtil jwtUtil) {
        this.subscriptionRepository = subscriptionRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract URI and determine if the request is public or login endpoint
        String requestUri = request.getRequestURI();
        LOGGER.info("Request URI: " + requestUri);

        if (isLoginRequest(requestUri)) {
            handleLoginRequest(request, response, filterChain);  // Handle login endpoint differently
        } else if (isPublicRequest(requestUri)) {
            handlePublicRequest(request, response, filterChain);  // Continue for public API
        } else {
            handleTenantRequest(request, response, filterChain, requestUri);  // Tenant request validation
        }
    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LOGGER.info("Login request, only validating tenant ID in path.");
        // Step 1: Extract tenant ID from the URL path (as we don't validate JWT here)
        String tenantId = extractTenantIdFromPath(request);

        if (tenantId == null) {
            handleErrorResponse(response, "Incorrect URL path. Tenant ID is missing.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Step 2: Validate the tenant ID in the subscription table
        boolean isValidTenant = subscriptionRepository.existsByTenantId(tenantId);

        if (!isValidTenant) {
            handleErrorResponse(response, "Invalid Tenant ID.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Step 3: Continue with the filter chain after tenant validation
        filterChain.doFilter(request, response);
    }

    private void handlePublicRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LOGGER.info("Public request, continuing filter chain.");
        filterChain.doFilter(request, response);  // Continue with the filter chain for public requests
    }

    private void handleTenantRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String requestUri)
            throws ServletException, IOException {

        // Step 2: Extract tenant ID from the JWT token
        String token = getJwtTokenFromRequest(request);
        String jwtTenantId = null;

        if (token != null) {
            try {
                jwtTenantId = jwtUtil.extractTenantId(token);
            } catch (JwtException e) {
                handleErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
                return;  // Stop processing further
            }
        }

        // Step 3: Extract tenant ID from the URL path
        String urlTenantId = extractTenantIdFromPath(request);

        if (urlTenantId == null) {
            handleErrorResponse(response, "Incorrect URL path. Tenant ID is missing.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Step 4: Compare tenant IDs from JWT and URL
        if (jwtTenantId != null && !jwtTenantId.equals(urlTenantId)) {
            handleErrorResponse(response, "Tenant ID mismatch between JWT token and request URI.", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Step 5: Validate the tenant ID in the subscription table
        boolean isValidTenant = subscriptionRepository.existsByTenantId(urlTenantId);

        if (!isValidTenant) {
            handleErrorResponse(response, "Invalid Tenant ID.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Step 6: Set the tenant ID in the TenantContext for use in other parts of the application
        TenantContext.setTenant(urlTenantId);

        // Step 7: Clean the URI to remove the tenant ID for spring security and controller processing
        String cleanedUri = cleanUri(requestUri);

        // Step 8: Forward the request with the cleaned URI
        request.getRequestDispatcher(cleanedUri).forward(request, response);
    }

    // Helper methods for extracting JWT token and tenant IDs

    private String getJwtTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Extract token from "Bearer <token>"
        }
        return null;
    }

    private String extractTenantIdFromPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String[] pathSegments = requestUri.split("/");

        // If the URI has at least 3 segments (tenantId/api/other), extract the tenantId
        if (pathSegments.length > 2) {
            return pathSegments[1];  // tenantId is the second segment (index 1)
        }

        return null;  // No tenantId found in the URI, return null
    }

    private String cleanUri(String requestUri) {
        String[] pathSegments = requestUri.split("/");
        if (pathSegments.length > 2) {
            // Remove the first segment (tenantId) from the URI
            StringBuilder cleanedUri = new StringBuilder();
            for (int i = 2; i < pathSegments.length; i++) {
                cleanedUri.append("/").append(pathSegments[i]);
            }
            return cleanedUri.toString();
        }

        return requestUri;  // If no tenant ID is in the path, return the original URI
    }

    private void handleErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);

        // Convert the map to a JSON string
        String jsonResponse = convertMapToJson(errorResponse);

        // Write the error response to the response output stream
        response.getWriter().write(jsonResponse);
    }

    private String convertMapToJson(Map<String, String> map) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        // Remove the trailing comma
        if (jsonBuilder.length() > 1) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private boolean isPublicRequest(String requestUri) {
        return requestUri.startsWith(PUBLIC_API_PREFIX);  // Use the public API prefix to match all paths starting with "/api/subscriptions"
    }

    private boolean isLoginRequest(String requestUri) {
        return requestUri.startsWith(LOGIN_API_PREFIX);  // Handle login endpoint separately
    }
}

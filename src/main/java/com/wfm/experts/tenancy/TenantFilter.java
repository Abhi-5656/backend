package com.wfm.experts.tenancy;

import com.wfm.experts.repository.core.SubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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

    private final SubscriptionRepository subscriptionRepository;

    // Injecting SubscriptionRepository directly
    public TenantFilter(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract URI and determine if the request is public
        String requestUri = request.getRequestURI();
        LOGGER.info("Request URI: " + requestUri);

        if (isPublicRequest(requestUri)) {
            handlePublicRequest(request, response, filterChain);
        } else {
            handleTenantRequest(request, response, filterChain, requestUri);
        }
    }

    /**
     * Handles requests that are public and don't require tenant validation.
     */
    private void handlePublicRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LOGGER.info("Public request, continuing filter chain.");
        filterChain.doFilter(request, response);  // Continue with the filter chain for public requests
    }

    /**
     * Handles requests that need tenant validation and processing.
     */
    private void handleTenantRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String requestUri)
            throws ServletException, IOException {

        // Step 2: Extract tenant ID from the request URI
        String tenantId = extractTenantIdFromPath(request);

        if (tenantId == null) {
            // If no tenantId is found in the URI, respond with an error
            handleErrorResponse(response, "Incorrect URL path. Tenant ID is missing.", HttpServletResponse.SC_BAD_REQUEST);
            return;  // Stop processing further
        }

        // Step 3: Validate the tenant ID in the subscription table
        boolean isValidTenant = subscriptionRepository.existsByTenantId(tenantId);

        if (!isValidTenant) {
            // If the tenant ID is invalid, return a 400 error with a message
            handleErrorResponse(response, "Invalid Tenant ID.", HttpServletResponse.SC_BAD_REQUEST);
            return;  // Stop processing further
        }

        // Step 4: Set the tenant ID in the TenantContext for use in other parts of the application
        TenantContext.setTenant(tenantId);

        // Step 5: Clean the URI to remove the tenant ID for spring security and controller processing
        String cleanedUri = cleanUri(requestUri);

        // Step 6: Forward the request with the cleaned URI
        request.getRequestDispatcher(cleanedUri).forward(request, response);
    }

    /**
     * Checks if the request URI is a public request (no tenant validation required).
     * Public URIs are those that start with "/api/subscriptions" and can have anything after that.
     */
    private boolean isPublicRequest(String requestUri) {
        return requestUri.startsWith(PUBLIC_API_PREFIX);  // Use the public API prefix to match all paths starting with "/api/subscriptions"
    }

    /**
     * Extracts the tenant ID from the URL path.
     * Assumes the tenant ID is the first part of the path after the domain.
     */
    private String extractTenantIdFromPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String[] pathSegments = requestUri.split("/");

        // If the URI has at least 3 segments (tenantId/api/other), extract the tenantId
        if (pathSegments.length > 2) {
            return pathSegments[1];  // tenantId is the second segment (index 1)
        }

        return null;  // No tenantId found in the URI, return null
    }

    /**
     * Cleans the URI by removing the tenant ID from it.
     */
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

    /**
     * Generates an error response with the given message and HTTP status code.
     */
    private void handleErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);

        // Convert the map to a JSON string
        String jsonResponse = convertMapToJson(errorResponse);

        // Write the error response to the response output stream
        response.getWriter().write(jsonResponse);
    }

    /**
     * Converts a Map to a JSON string.
     */
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


}

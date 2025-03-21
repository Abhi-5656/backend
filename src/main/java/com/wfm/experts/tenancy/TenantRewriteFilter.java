package com.wfm.experts.tenancy;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * ✅ This filter extracts the tenant ID from the first path segment.
 * ✅ It rewrites the request URI to remove the tenant segment for cleaner API handling.
 */

@Component
public class TenantRewriteFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(TenantRewriteFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI(); // Example: "/nextgen-solutions-inc/api/auth/login"

        // Skip public endpoints (no tenant extraction needed)
        if (isPublicRequest(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        // If the URI has already been cleaned, skip cleaning
        if (httpRequest.getAttribute("tenantIdProcessed") != null) {
            chain.doFilter(request, response);
            return;
        }

        // Extract tenantId from the first path segment
        String[] pathParts = requestUri.split("/");
        if (pathParts.length > 2) {
            String tenantId = pathParts[1]; // Extracting tenantId from URL
            LOGGER.info("✅ Extracted Tenant ID: " + tenantId);

            // Store the tenant ID in the TenantContext
            TenantContext.setTenant(tenantId);

            // Clean the URI path by removing the tenantId
            String newPath = requestUri.replaceFirst("/" + tenantId, "");
            LOGGER.info("🔹 Cleaned-up path: " + newPath);

            // Set an attribute to indicate the tenant ID has been processed
            httpRequest.setAttribute("tenantIdProcessed", true);

            // Wrap the request with the cleaned-up path and forward it
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getRequestURI() {
                    return newPath; // Return the cleaned-up path (e.g., "/api/auth/login")
                }
            };

            chain.doFilter(wrappedRequest, response);  // Forward the cleaned request to the next filter/handler
        } else {
            LOGGER.warning("❌ No tenant segment found in URL: " + requestUri);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant ID missing in path.");
        }
    }

    // Determine if the request is public (no tenant validation)
    private boolean isPublicRequest(String requestUri) {
        return requestUri.startsWith("/api/subscriptions"); // Public endpoints (like /subscriptions) are allowed without tenant validation
    }
}









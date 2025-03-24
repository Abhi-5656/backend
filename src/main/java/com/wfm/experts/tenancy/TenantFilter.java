package com.wfm.experts.tenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.exception.JwtAuthenticationException;
import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.security.JwtUtil;
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
    private static final String PUBLIC_API_PREFIX = "/api/subscriptions";
    private static final String LOGIN_API_PREFIX = "/api/auth/login";

    private final SubscriptionRepository subscriptionRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();  // ✅ JSON Serializer

    public TenantFilter(SubscriptionRepository subscriptionRepository, JwtUtil jwtUtil) {
        this.subscriptionRepository = subscriptionRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        LOGGER.info("Processing request URI: " + requestUri);

        if (isLoginRequest(requestUri)) {
            handleLoginRequest(request, response, filterChain);
        } else if (isPublicRequest(requestUri)) {
            handlePublicRequest(request, response, filterChain);
        } else {
            handleTenantRequest(request, response, filterChain, requestUri);
        }
    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LOGGER.info("Login request, extracting tenant ID from URL.");

        String tenantId = extractTenantIdFromPath(request);

        if (tenantId == null) {
            sendErrorResponse(response, "Tenant ID is missing in URL.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!subscriptionRepository.existsByTenantId(tenantId)) {
            sendErrorResponse(response, "Invalid Tenant ID.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handlePublicRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LOGGER.info("Public API request, proceeding without validation.");
        filterChain.doFilter(request, response);
    }

    private void handleTenantRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String requestUri)
            throws ServletException, IOException {

        String token = getJwtTokenFromRequest(request);
        String jwtTenantId = null;

        if (token != null) {
            try {
                jwtTenantId = jwtUtil.extractTenantId(token);
            } catch (JwtAuthenticationException e) {
                LOGGER.severe("JWT Validation Failed: " + e.getMessage());
                sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        String urlTenantId = extractTenantIdFromPath(request);
        if (urlTenantId == null) {
            sendErrorResponse(response, "Tenant ID is missing in URL.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (jwtTenantId != null && !jwtTenantId.equals(urlTenantId)) {
            LOGGER.warning("Tenant ID mismatch! Token: " + jwtTenantId + ", URL: " + urlTenantId);
            sendErrorResponse(response, "Tenant ID mismatch between JWT and URL.", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!subscriptionRepository.existsByTenantId(urlTenantId)) {
            sendErrorResponse(response, "Invalid Tenant ID.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TenantContext.setTenant(urlTenantId);
        String cleanedUri = cleanUri(requestUri);
        request.getRequestDispatcher(cleanedUri).forward(request, response);
    }

    // 🔹 Helper methods

    private String getJwtTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractTenantIdFromPath(HttpServletRequest request) {
        String[] pathSegments = request.getRequestURI().split("/");
        return (pathSegments.length > 2) ? pathSegments[1] : null;
    }

    private String cleanUri(String requestUri) {
        String[] pathSegments = requestUri.split("/");
        if (pathSegments.length > 2) {
            StringBuilder cleanedUri = new StringBuilder();
            for (int i = 2; i < pathSegments.length; i++) {
                cleanedUri.append("/").append(pathSegments[i]);
            }
            return cleanedUri.toString();
        }
        return requestUri;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }

    private boolean isPublicRequest(String requestUri) {
        return requestUri.startsWith(PUBLIC_API_PREFIX);
    }

    private boolean isLoginRequest(String requestUri) {
        return requestUri.startsWith(LOGIN_API_PREFIX);
    }
}

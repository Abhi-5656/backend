/*
 *
 *  * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 *  *
 *  * This software, including all associated files, documentation, and related materials,
 *  * is the proprietary property of WFM EXPERTS INDIA PVT LTD. Unauthorized copying,
 *  * distribution, modification, or any form of use beyond the granted permissions
 *  * without prior written consent is strictly prohibited.
 *  *
 *  * DISCLAIMER:
 *  * This software is provided "as is," without warranty of any kind, express or implied,
 *  * including but not limited to the warranties of merchantability, fitness for a particular
 *  * purpose, and non-infringement.
 *  *
 *  * For inquiries, contact legal@wfmexperts.com.
 *
 */

package com.wfm.experts.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.exception.JwtAuthenticationException;
import com.wfm.experts.util.TenantSchemaUtil;
import com.wfm.experts.tenancy.TenantContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TenantSchemaUtil tenantSchemaUtil;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON Serializer

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Lazy UserDetailsService userDetailsService,
            TenantSchemaUtil tenantSchemaUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    /**
     * Skip JWT authentication for login (`/api/auth/login`) and subscription (`/api/subscriptions`) endpoints
     */
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String requestUri = request.getRequestURI();
//        return requestUri.startsWith("/api/auth/login") || requestUri.startsWith("/api/subscriptions");
//    }

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String requestUri = request.getRequestURI();
//
//        return requestUri.startsWith("/api/auth/login")
//                || requestUri.startsWith("/api/subscriptions")
//                || (requestUri.contains("/public/jobs/") && requestUri.endsWith("/apply"));
//
//    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        return requestUri.startsWith("/api/auth/login")
                || requestUri.startsWith("/api/subscriptions")
                || (requestUri.contains("/public/jobs/") && requestUri.endsWith("/apply"))
                || requestUri.startsWith("/ws"); // ✅ WebSocket endpoint excluded

    }




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            LOGGER.warning("Missing Authorization Token!");
            sendErrorResponse(response, "Authorization token is required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            // 🔹 Extract Tenant ID and Set It in Context
            String tenantId = jwtUtil.extractTenantId(token);
            TenantContext.setTenant(tenantId);

            // 🔹 Extract Email and Validate Token
            String email = jwtUtil.extractEmail(token);
            jwtUtil.validateToken(token, email);  // ✅ Now validates token against the extracted email

            // 🔹 Load User Details and Authenticate
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (userDetails == null) {
                throw new JwtAuthenticationException("User not found with email: " + email);
            }

            // 🔹 Set User Authentication in Security Context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtAuthenticationException e) {
            LOGGER.severe("JWT Authentication Failed: " + e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Helper Method to Send JSON Error Responses
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

package com.wfm.experts.security;

import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Custom JWT Authentication filter for extracting tenantId and authenticating the user.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TenantSchemaUtil tenantSchemaUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, TenantSchemaUtil tenantSchemaUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // If no "Bearer" token is present, skip further processing
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7); // Extract token by removing "Bearer "

        try {
            // Extract tenantId from JWT token and set it in the context for tenant schema switching
            UUID tenantId = extractTenantIdFromJwt(token);
            TenantContext.setTenant(tenantId); // Store tenant context

            // Extract the user email from JWT token
            String email = jwtUtil.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails == null) {
                throw new JwtException("User not found with email: " + email);
            }

            // Authenticate the user and set SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Proceed with the schema switch logic
            tenantSchemaUtil.switchToTenantSchema();

        } catch (JwtException | IllegalArgumentException e) {
            // If token is invalid, set the response to unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired JWT token");
            return;
        }

        chain.doFilter(request, response); // Continue with the next filter in the chain
    }

    /**
     * Extract tenant_id from the JWT token.
     */
    private UUID extractTenantIdFromJwt(String token) {
        try {
            return jwtUtil.extractTenantId(token); // Extract tenant ID from JWT
        } catch (Exception e) {
            throw new JwtException("Failed to extract tenantId from JWT: " + e.getMessage());
        }
    }
}

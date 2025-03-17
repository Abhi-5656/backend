package com.wfm.experts.security;

import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
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

/**
 * ✅ JWT Authentication Filter - Extracts token, verifies user, and sets security context.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final TenantSchemaUtil tenantSchemaUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
                                   AuthenticationManager authenticationManager, TenantSchemaUtil tenantSchemaUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

        try {
            // ✅ Extract details from JWT
            String email = jwtUtil.extractEmail(token);
            UUID tenantId = jwtUtil.extractTenantId(token);

            if (email == null || tenantId == null) {
                throw new RuntimeException("❌ Invalid token! Email or Tenant ID is missing.");
            }

            // ✅ Validate token
            if (!jwtUtil.validateToken(token, email)) {
                throw new RuntimeException("❌ Token validation failed for user: " + email);
            }

            // ✅ Set Tenant Context
            TenantContext.setTenant(tenantId);

            // ✅ Switch Schema - Validate before switching
            tenantSchemaUtil.switchToTenantSchema();

            // ✅ Load user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (userDetails == null) {
                throw new RuntimeException("❌ User not found: " + email);
            }

            // ✅ Authenticate user with Spring Security
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            System.out.println("✅ Authenticated User: " + email + " in Tenant Schema.");

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("❌ Token expired! Please login again.", e);
        } catch (JwtException e) {
            throw new RuntimeException("❌ Invalid JWT Token! Cannot authenticate user.", e);
        } catch (Exception e) {
            throw new RuntimeException("❌ Authentication failed: " + e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }
}

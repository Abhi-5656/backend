package com.wfm.experts.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * ✅ Utility class for generating and validating JWT tokens.
 * ✅ Supports multi-tenancy by embedding `tenantId`, `email`, and `role` in the token.
 */
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your-very-secure-secret-key-should-be-32bytes";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * ✅ Generates a JWT Access Token with multi-tenant details.
     *
     * @param email    Employee's email (now used instead of employeeId)
     * @param tenantId Tenant ID (UUID)
     * @param role     Employee Role (ADMIN, MANAGER, EMPLOYEE, etc.)
     * @return JWT Token
     */
    public String generateToken(String email, UUID tenantId, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId.toString())  // ✅ Store UUID as String in JWT
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Generates a Refresh Token for renewing access tokens.
     *
     * @param email Employee's email
     * @return Refresh Token
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extracts Claims from a JWT Token.
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ Extract Email from JWT (Previously `extractEmployeeId`)
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * ✅ Extract Tenant ID from JWT as `UUID`
     */
    public UUID extractTenantId(String token) {
        String tenantIdString = extractClaims(token).get("tenantId", String.class);
        return UUID.fromString(tenantIdString);  // ✅ Convert back to UUID
    }

    /**
     * ✅ Extract Role from JWT
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * ✅ Checks if JWT Token is Expired
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * ✅ Validates JWT Token
     */
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    /**
     * ✅ Retrieves the Expiration Date of a JWT Token
     */
    public Date getTokenExpiryDate(String token) {
        return extractClaims(token).getExpiration();
    }
}

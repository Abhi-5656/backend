package com.wfm.experts.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * ✅ Utility class for generating and validating JWT tokens.
 * ✅ Supports multi-tenancy by embedding `tenantId`, `email`, and `role` in the token.
 */
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your-very-secure-secret-key-must-be-32bytes";  // 🔹 Use .env or config file in production
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 10;  // 🔹 10 hours
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;  // 🔹 7 days

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());  // 🔹 Generates secure 256-bit key

    /**
     * ✅ Generates a JWT Access Token with multi-tenant details.
     *
     * @param email    User's email
     * @param tenantId Tenant ID (UUID)
     * @param role     User Role (ADMIN, MANAGER, EMPLOYEE, etc.)
     * @return JWT Access Token
     */
    public String generateToken(String email, UUID tenantId, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId.toString())  // 🔹 Store UUID as String
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extracts Claims from a JWT Token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ Generates a Refresh Token for renewing access tokens.
     *
     * @param email User's email
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
     * ✅ Generic method to extract a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * ✅ Extracts Email from JWT.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * ✅ Extracts Tenant ID from JWT as `UUID`.
     */
    public UUID extractTenantId(String token) {
        String tenantIdString = extractClaim(token, claims -> claims.get("tenantId", String.class));
        return UUID.fromString(tenantIdString);  // 🔹 Convert back to UUID
    }

    /**
     * ✅ Extracts Role from JWT.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * ✅ Checks if JWT Token is Expired.
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * ✅ Validates JWT Token.
     */
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    /**
     * ✅ Retrieves the Expiration Date of a JWT Token.
     */
    public Date getTokenExpiryDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

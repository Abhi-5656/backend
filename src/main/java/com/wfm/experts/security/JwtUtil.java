package com.wfm.experts.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * ✅ Utility class for generating and validating JWT tokens.
 * ✅ Supports multi-tenancy by embedding `tenantId`, `email`, and `role` in the token.
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    @Value("${jwt.secret}")  // 🔹 Load from application.properties or .env
    private String secretKey;

    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 10;  // 🔹 10 hours
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;  // 🔹 7 days

    /**
     * ✅ Generates a secure key from the configured secret.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * ✅ Generates a JWT Access Token with multi-tenant details.
     *
     * @param email    User's email
     * @param tenantId Tenant ID (String-based)
     * @param role     User Role (ADMIN, MANAGER, EMPLOYEE, etc.)
     * @return JWT Access Token
     */
    public String generateToken(String email, String tenantId, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId)  // 🔹 Store as String
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extracts Claims from a JWT Token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())  // 🔹 Secure parsing
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            LOGGER.warning("JWT parsing failed: " + e.getMessage());
            throw new JwtException("Invalid JWT token");
        }
    }

    /**
     * ✅ Generates a Refresh Token for renewing access tokens.
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
     * ✅ Extracts Tenant ID from JWT as **String**.
     */
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));  // 🔹 No UUID conversion
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
        try {
            final String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(email) && !isTokenExpired(token));
        } catch (JwtException e) {
            LOGGER.warning("JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Retrieves the Expiration Date of a JWT Token.
     */
    public Date getTokenExpiryDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

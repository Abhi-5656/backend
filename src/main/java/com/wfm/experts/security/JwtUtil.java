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

import com.wfm.experts.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating and validating JWT tokens.
 * Supports multi-tenancy by embedding `tenantId`, `email`, and `role` in the token.
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    @Value("${jwt.secret}")  // 🔹 Secure key loaded from properties
    private String secretKey;

    @Value("${jwt.expiration}")  // 🔹 15 minutes expiration (900000 ms)
    private long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")  // 🔹 30 minutes expiration (1800000 ms)
    private long refreshTokenExpiration;

    /**
     * ✅ Generates a secure signing key from the configured secret.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * ✅ Generates a JWT Access Token with expiration time (15 minutes).
     */
//    public String generateToken(String email, String tenantId, String role) {
//        Date expirationDate = new Date(System.currentTimeMillis() + accessTokenExpiration);
//        String expiresIn = calculateExpiresIn(expirationDate);
//
//        return Jwts.builder()
//                .setSubject(email)
//                .claim("tenantId", tenantId)
//                .claim("role", role)
//                .claim("expiresIn", expiresIn)  // ✅ Human-readable expiration time (e.g., "15 minutes")
//                .setIssuedAt(new Date())
//                .setExpiration(expirationDate)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
    public String generateToken(String email, String tenantId, List<String> roles, String fullName) {
        Date expirationDate = new Date(System.currentTimeMillis() + accessTokenExpiration);
        String expiresIn = calculateExpiresIn(expirationDate);

        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId)
                .claim("role", roles)            // <--- Array of roles
                .claim("fullName", fullName)
                .claim("expiresIn", expiresIn)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Generates a Refresh Token with expiration time (30 minutes).
     */
    public String generateRefreshToken(String email) {
        Date expirationDate = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        String expiresIn = calculateExpiresIn(expirationDate);

        return Jwts.builder()
                .setSubject(email)
                .claim("expiresIn", expiresIn)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ Extracts Claims from a JWT Token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            LOGGER.severe("JWT Token is expired: " + e.getMessage());
            throw new JwtAuthenticationException("JWT token is expired", e);
        } catch (MalformedJwtException e) {
            LOGGER.severe("Invalid JWT token: " + e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token", e);
        } catch (SignatureException e) {
            LOGGER.severe("Invalid JWT signature: " + e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT signature", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.severe("JWT token is unsupported: " + e.getMessage());
            throw new JwtAuthenticationException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            LOGGER.severe("JWT claims string is empty: " + e.getMessage());
            throw new JwtAuthenticationException("JWT claims string is empty", e);
        }
    }

    /**
     * ✅ Extracts a specific claim from the token.
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
    public void validateToken(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            if (!extractedEmail.equals(email)) {
                LOGGER.warning("Token email does not match expected email.");
                throw new JwtAuthenticationException("Invalid JWT token: Email mismatch");
            }
            if (isTokenExpired(token)) {
                throw new JwtAuthenticationException("JWT token is expired");
            }
        } catch (JwtAuthenticationException e) {
            throw e;  // Rethrow for global handler to catch
        } catch (Exception e) {
            LOGGER.warning("Unexpected JWT validation error: " + e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token", e);
        }
    }

    /**
     * ✅ Retrieves the Expiration Date of a JWT Token.
     */
    public Date getTokenExpiryDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * ✅ Converts expiration time to a human-readable format (e.g., "15 minutes").
     */
    private String calculateExpiresIn(Date expiryDate) {
        long millisLeft = expiryDate.getTime() - System.currentTimeMillis();
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisLeft);

        return minutesLeft + " minutes";
    }
}

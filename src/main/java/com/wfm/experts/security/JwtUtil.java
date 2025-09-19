// harshwfm/wfm-backend/HarshWfm-wfm-backend-505d16993db54f997bfe6d610d42bf01feca5578/src/main/java/com/wfm/experts/security/JwtUtil.java
package com.wfm.experts.security;

import com.wfm.experts.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

@Component
public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a JWT Access Token.
     */
    public String generateToken(String email, String tenantId, List<String> roles, String fullName, String employeeId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId)
                .claim("role", roles)
                .claim("fullName", fullName)
                .claim("employeeId", employeeId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a Refresh Token.
     */
    public String generateRefreshToken(String email, String tenantId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("tenantId", tenantId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            // This is where the error occurs if the token is null or empty
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("JWT token string is empty or null.");
            }
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token is expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            // Catches MalformedJwtException, SignatureException, etc.
            throw new JwtAuthenticationException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));
    }

    public String extractEmployeeId(String token) {
        return extractClaim(token, claims -> claims.get("employeeId", String.class));
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("role", List.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtAuthenticationException e) {
            // If the token is invalid for any reason (malformed, expired, etc.),
            // we can consider it "expired" for validation purposes.
            return true;
        }
    }

    public void validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        if (!extractedEmail.equals(email) || isTokenExpired(token)) {
            throw new JwtAuthenticationException("JWT token is invalid or expired.");
        }
    }

    public String getExpiresIn(String token) {
        Date expirationDate = extractExpiration(token);
        long millisLeft = expirationDate.getTime() - System.currentTimeMillis();
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisLeft);
        return minutesLeft + " minutes";
    }
}
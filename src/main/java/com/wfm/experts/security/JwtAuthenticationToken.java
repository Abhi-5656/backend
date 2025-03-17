package com.wfm.experts.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

/**
 * ✅ Custom Authentication Token for JWT-based authentication.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String token;

    public JwtAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true); // ✅ Mark authentication as successful
    }

    @Override
    public Object getCredentials() {
        return token; // ✅ Return JWT Token as credentials
    }

    @Override
    public Object getPrincipal() {
        return principal; // ✅ Return the authenticated user
    }
}

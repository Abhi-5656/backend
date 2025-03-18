package com.wfm.experts.dto.tenant.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * ✅ DTO for authentication response containing JWT token details.
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String token;        // ✅ JWT access token
//    private String refreshToken; // ✅ Refresh token for renewing access
    private Date expiryDate;     // ✅ Token expiration time
    private String tokenType;    // ✅ Usually "Bearer"
}

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

package com.wfm.experts.dto.tenant.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * DTO for authentication response containing JWT token details.
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String token; // ✅ JWT access token
    private String tokenType; // ✅ Usually "Bearer"
    private String expiresIn; // ✅ Readable expiration duration (e.g., "15 minutes")

    /**
     * Constructor to auto-calculate `expiresIn` field based on expiration time.
     */
    public AuthResponse(String token, String tokenType, Date expiryDate) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = calculateExpiresIn(expiryDate);
    }

    /**
     * Converts expiration time to a human-readable format (e.g., "15 minutes").
     */
    private String calculateExpiresIn(Date expiryDate) {
        long millisLeft = expiryDate.getTime() - System.currentTimeMillis();
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millisLeft);

        return minutesLeft + " minutes";
    }
}
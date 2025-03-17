package com.wfm.experts.dto.tenant.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * ✅ DTO for Authentication Request (Login)
 * - Uses `email` instead of `employeeId` for multi-tenancy support.
 * - Tenant ID is auto-extracted from the JWT token (No need to pass manually).
 */
@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;  // 🔹 Use email instead of employee ID

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}

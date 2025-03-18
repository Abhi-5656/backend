package com.wfm.experts.controller;

import com.wfm.experts.dto.tenant.common.AuthRequest;
import com.wfm.experts.dto.tenant.common.AuthResponse;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.service.TenantResolverService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.UUID;

/**
 * ✅ Authentication Controller for handling login and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TenantSchemaUtil tenantSchemaUtil;

    @Autowired
    private TenantResolverService tenantResolverService;  // ✅ Resolves `tenant_id` dynamically

    /**
     * ✅ Login API - Authenticate using email & password, then return JWT Token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {

        // ✅ Extract tenant_id dynamically from email
        UUID tenantId = tenantResolverService.resolveTenantId(request.getEmail());
        if (tenantId == null) {
            throw new RuntimeException("❌ Tenant ID not found for this email.");
        }

        // ✅ Store Tenant ID in Context and Switch Schema
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.switchToTenantSchema();

        // ✅ Authenticate the user within the tenant schema
        Employee employee = employeeService.authenticateByEmail(request.getEmail(), request.getPassword());

        // ✅ Generate JWT Token with `tenantId`
        String token = jwtUtil.generateToken(
                employee.getEmail(),
                tenantId,  // ✅ Extracted dynamically
                employee.getRole().getRoleName()
        );

        // ✅ Generate Refresh Token
        String refreshToken = jwtUtil.generateRefreshToken(employee.getEmail());

        // ✅ Get Token Expiry Date
        Date expiryDate = jwtUtil.getTokenExpiryDate(token);

        return ResponseEntity.ok(new AuthResponse(token, expiryDate, "Bearer"));
    }


}

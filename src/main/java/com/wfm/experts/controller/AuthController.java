package com.wfm.experts.controller;

import com.wfm.experts.dto.tenant.common.AuthRequest;
import com.wfm.experts.dto.tenant.common.AuthResponse;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.Optional;

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
    private PasswordEncoder passwordEncoder;

    /**
     * ✅ Login API - Authenticate using email & password, then return JWT Token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {

        // ✅ Get tenant ID from context (set by TenantInterceptor)
        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new RuntimeException("❌ Tenant ID not found in context.");
        }

        // ✅ Load user from UserDetailsService (EmployeeService)
        UserDetails userDetails = employeeService.loadUserByUsername(request.getEmail());

        // ✅ Retrieve the Employee from the database (using email from userDetails)
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmail(request.getEmail());
        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("❌ Employee not found!");
        }

        Employee employee = employeeOpt.get();

        // ✅ Validate password
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new RuntimeException("❌ Invalid email or password!");
        }

        // ✅ Generate JWT Token
        String token = jwtUtil.generateToken(employee.getEmail(), tenantId, employee.getRole().getRoleName());


        // ✅ Get Token Expiry Date
        Date expiryDate = jwtUtil.getTokenExpiryDate(token);

        return ResponseEntity.ok(new AuthResponse(token, expiryDate, "Bearer"));
    }

}

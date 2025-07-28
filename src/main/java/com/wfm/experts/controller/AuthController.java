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

package com.wfm.experts.controller;

import com.wfm.experts.dto.tenant.common.AuthRequest;
import com.wfm.experts.dto.tenant.common.AuthResponse;
import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.Role;
import com.wfm.experts.exception.*;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Authentication Controller for handling login and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Login API - Authenticate using email & password, then return JWT Token.
     */
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody  AuthRequest request) {
//
//        // ✅ Validate if email or password is `null`
//        if (request.getEmail() == null || request.getPassword() == null) {
//            throw new NullCredentialsException("Username or password cannot be null.");
//        }
//
//        // ✅ Validate if email or password is empty (`""`)
//        if (request.getEmail().trim().isEmpty()) {
//            throw new EmptyUsernameException("Username cannot be empty.");
//        }
//        if (request.getPassword().trim().isEmpty()) {
//            throw new EmptyPasswordException("Password cannot be empty.");
//        }
//
//        // ✅ Get tenant ID from context
//        String tenantId = TenantContext.getTenant();
//        if (tenantId == null) {
//            throw new RuntimeException("Tenant ID not found in context.");
//        }
//
//        // ✅ Load user details
//        UserDetails userDetails = employeeService.loadUserByUsername(request.getEmail());
//
//        // ✅ Retrieve Employee from database
//        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmail(request.getEmail());
//        if (employeeOpt.isEmpty()) {
//            throw new InvalidEmailException("Email not found: " + request.getEmail());
//        }
//
//        Employee employee = employeeOpt.get();
//
//        // ✅ Validate password
//        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
//            throw new InvalidPasswordException("Invalid password for email: " + request.getEmail());
//        }
//
//        // ✅ Generate JWT Token
//        String token = jwtUtil.generateToken(employee.getEmail(), tenantId, employee.getRole().getRoleName());
//
//        // ✅ Get Token Expiry Date
//        Date expiryDate = jwtUtil.getTokenExpiryDate(token);
//
//        // ✅ Return response without expiryDate (only `expiresIn`)
//        return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiryDate));
//    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        // --- Input Validation ---
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new NullCredentialsException("Username or password cannot be null.");
        }
        if (request.getEmail().trim().isEmpty()) {
            throw new EmptyUsernameException("Username cannot be empty.");
        }
        if (request.getPassword().trim().isEmpty()) {
            throw new EmptyPasswordException("Password cannot be empty.");
        }

        // --- Tenant Context ---
        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context.");
        }

        // --- Load User and Employee Info ---
        employeeService.loadUserByUsername(request.getEmail()); // throws if not found

        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmail(request.getEmail());
        if (employeeOpt.isEmpty()) {
            throw new InvalidEmailException("Email not found: " + request.getEmail());
        }
        Employee employee = employeeOpt.get();

        // --- Password Validation ---
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new InvalidPasswordException("Invalid password for email: " + request.getEmail());
        }

        // --- Full Name (safe concatenation, trims, handles nulls) ---
        String fullName = "";
        if (employee.getPersonalInfo() != null) {
            String first = employee.getPersonalInfo().getFirstName();
            String last = employee.getPersonalInfo().getLastName();
            fullName = ((first != null ? first.trim() : "") + " " + (last != null ? last.trim() : "")).trim();
        }

        // --- Multi-Role (getRole() should be getRoles()) ---
        List<String> roles = (employee.getRoles() != null)
                ? employee.getRoles().stream()
                .map(Role::getRoleName)
                .filter(Objects::nonNull)
                .toList()
                : List.of();

        // --- Generate JWT Token ---
        String token = jwtUtil.generateToken(employee.getEmail(), tenantId, roles, fullName);

        // --- Expires In ---
        String expiresIn = jwtUtil.extractClaim(token, claims -> claims.get("expiresIn", String.class));

        return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn));
    }
    
}

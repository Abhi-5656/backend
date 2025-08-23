package com.wfm.experts.controller;

import com.wfm.experts.tenant.common.auth.AuthRequest;
import com.wfm.experts.tenant.common.auth.AuthResponse;
import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.entity.Role;
import com.wfm.experts.exception.*;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.tenant.common.employees.mapper.EmployeeMapper;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeMapper employeeMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new NullCredentialsException("Username or password cannot be null.");
        }
        if (request.getEmail().trim().isEmpty()) {
            throw new EmptyUsernameException("Username cannot be empty.");
        }
        if (request.getPassword().trim().isEmpty()) {
            throw new EmptyPasswordException("Password cannot be empty.");
        }

        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context.");
        }

        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidEmailException("Email not found: " + request.getEmail()));

        // The password from the DTO is not available, so we need to fetch the entity to verify the password
        Employee employeeEntity = employeeService.getEmployeeByEmail(request.getEmail())
                .map(employeeMapper::toEntity) // Assuming you add a method to convert DTO to entity in your service/mapper
                .orElseThrow(() -> new InvalidEmailException("Email not found: " + request.getEmail()));

        if (employeeEntity.getPassword() == null || employeeEntity.getPassword().trim().isEmpty()) {
            throw new InvalidPasswordException("Invalid password for email: " + request.getEmail());
        }


        if (!passwordEncoder.matches(request.getPassword(), employeeEntity.getPassword())) {
            throw new InvalidPasswordException("Invalid password for email: " + request.getEmail());
        }


        String fullName = "";
        if (employeeDTO.getPersonalInfo() != null) {
            String first = employeeDTO.getPersonalInfo().getFirstName();
            String last = employeeDTO.getPersonalInfo().getLastName();
            fullName = ((first != null ? first.trim() : "") + " " + (last != null ? last.trim() : "")).trim();
        }

        List<String> roles = employeeDTO.getRoles();

        String accessToken = jwtUtil.generateToken(employeeDTO.getEmail(), tenantId, roles, fullName);
        String refreshToken = jwtUtil.generateRefreshToken(employeeDTO.getEmail(), tenantId);
        String expiresIn = jwtUtil.getExpiresIn(accessToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, "Bearer", expiresIn, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // Added logging to diagnose the issue
        logger.info("Attempting to refresh token: '{}'", refreshToken);

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.error("Refresh token is missing from the request body.");
            throw new JwtAuthenticationException("Refresh token is missing.");
        }

        try {
            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new JwtAuthenticationException("Refresh token is expired.");
            }

            String email = jwtUtil.extractEmail(refreshToken);
            String tenantId = jwtUtil.extractTenantId(refreshToken);

            EmployeeDTO employee = employeeService.getEmployeeByEmail(email)
                    .orElseThrow(() -> new InvalidEmailException("Email not found: " + email));

            String fullName = "";
            if (employee.getPersonalInfo() != null) {
                String first = employee.getPersonalInfo().getFirstName();
                String last = employee.getPersonalInfo().getLastName();
                fullName = ((first != null ? first.trim() : "") + " " + (last != null ? last.trim() : "")).trim();
            }

            List<String> roles = employee.getRoles();

            String newAccessToken = jwtUtil.generateToken(email, tenantId, roles, fullName);
            String newRefreshToken = jwtUtil.generateRefreshToken(email, tenantId); // Issue a new refresh token
            String expiresIn = jwtUtil.getExpiresIn(newAccessToken);

            return ResponseEntity.ok(new AuthResponse(newAccessToken, "Bearer", expiresIn, newRefreshToken));

        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid refresh token received.", e);
            throw new JwtAuthenticationException("Invalid refresh token.", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful. Please remove your tokens on the client side."));
    }
}
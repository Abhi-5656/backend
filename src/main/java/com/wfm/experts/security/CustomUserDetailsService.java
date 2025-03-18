package com.wfm.experts.security;

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * ✅ Custom implementation of `UserDetailsService` to load Employee details for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final TenantSchemaUtil tenantSchemaUtil;

    public CustomUserDetailsService(EmployeeRepository employeeRepository, JwtUtil jwtUtil, TenantSchemaUtil tenantSchemaUtil) {
        this.employeeRepository = employeeRepository;
        this.jwtUtil = jwtUtil;
        this.tenantSchemaUtil = tenantSchemaUtil;
    }

    /**
     * ✅ Loads Employee details by Email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ✅ Retrieve tenant context
        UUID tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID is missing. Cannot authenticate user!");
        }

        // ✅ Ensure the schema switch is successful
        try {
            tenantSchemaUtil.switchToTenantSchema();
        } catch (Exception e) {
            throw new RuntimeException("Error switching schema for Tenant ID: " + tenantId + " | " + e.getMessage());
        }

        // ✅ Fetch employee details from database
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (optionalEmployee.isEmpty()) {
            throw new UsernameNotFoundException("Employee not found with email: " + email);
        }

        Employee employee = optionalEmployee.get();

        // ✅ Return user details with authorities
        return new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                employee.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(employee.getRole().getRoleName()))
        );
    }
}

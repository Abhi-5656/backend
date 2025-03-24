package com.wfm.experts.service.impl;

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.exception.InvalidEmailException;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *Implements `UserDetailsService` for Spring Security & CRUD operations for Employees.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final TenantSchemaUtil tenantSchemaUtil;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder here

    // Constructor Injection of PasswordEncoder
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, TenantSchemaUtil tenantSchemaUtil, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.tenantSchemaUtil = tenantSchemaUtil;
        this.passwordEncoder = passwordEncoder; // Assign the password encoder here
    }
    /**
     * Loads an Employee by email for authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws InvalidEmailException {
        ensureSchemaSwitch();

        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (optionalEmployee.isEmpty()) {
            throw new InvalidEmailException("Employee not found with email: " + email);
        }

        Employee employee = optionalEmployee.get();
        return new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                employee.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(employee.getRole().getRoleName()))
        );
    }



    /**
     * ✅ Create a new Employee.
     */
    @Transactional
    @Override
    public Employee createEmployee(Employee employee) {
        ensureSchemaSwitch();
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    /**
     * ✅ Get Employee by Email.
     */
    @Override
    public Optional<Employee> getEmployeeByEmail(String email) {
        ensureSchemaSwitch();
        return employeeRepository.findByEmail(email);
    }

    /**
     * ✅ Get All Employees.
     */
    @Override
    public List<Employee> getAllEmployees() {
        ensureSchemaSwitch();
        return employeeRepository.findAll();
    }

    /**
     * ✅ Update an Employee by Email.
     */
    @Transactional
    @Override
    public Employee updateEmployee(String email, Employee updatedEmployee) {
        ensureSchemaSwitch();
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));

        existingEmployee.setFirstName(updatedEmployee.getFirstName());
        existingEmployee.setLastName(updatedEmployee.getLastName());
        existingEmployee.setPhoneNumber(updatedEmployee.getPhoneNumber());
        existingEmployee.setRole(updatedEmployee.getRole());

        return employeeRepository.save(existingEmployee);
    }

    /**
     * ✅ Delete an Employee by Email.
     */
    @Transactional
    @Override
    public void deleteEmployee(String email) {
        ensureSchemaSwitch();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found: " + email));
        employeeRepository.delete(employee);
    }

    /**
     * ✅ Ensure the schema is switched before any DB operation.
     */
    private void ensureSchemaSwitch() {
        tenantSchemaUtil.ensureTenantSchemaIsSet();
    }
}

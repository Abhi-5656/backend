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

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Employee Controller - Provides CRUD APIs for Employees.
 * Automatically switches to the correct schema based on JWT token.
 */
@RestController
@RequestMapping("/api/employees") // 🔹 No need to manually pass `tenant` in URL
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TenantSchemaUtil tenantSchemaUtil;

    /**
     * Extracts `tenantId` from JWT token & switches schema
     */
    private void setTenantSchemaFromToken(String token) {
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7); // ✅ Remove "Bearer " prefix
        }
        String tenantId = jwtUtil.extractTenantId(token); // ✅ Extract Tenant ID from JWT

        // ✅ Set Tenant Context (Important for Schema Switching)
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.ensureTenantSchemaIsSet(); // ✅ Ensure schema switch
    }

    /**
     * Create a new Employee (Requires JWT Token)
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestHeader("Authorization") String token,
                                                   @RequestBody @Valid Employee employee) {
        setTenantSchemaFromToken(token); // ✅ Auto-switch tenant schema
        Employee savedEmployee = employeeService.createEmployee(employee);
        return ResponseEntity.ok(savedEmployee);
    }

    /**
     * Get Employee by Email (Requires JWT Token)
     */
    @GetMapping("/{email}")
    public ResponseEntity<Employee> getEmployeeByEmail(@RequestHeader("Authorization") String token,
                                                       @PathVariable String email) {
        setTenantSchemaFromToken(token);
        Optional<Employee> employee = employeeService.getEmployeeByEmail(email);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update Employee by Email (Requires JWT Token)
     */
    @PutMapping("/{email}")
    public ResponseEntity<Employee> updateEmployee(@RequestHeader("Authorization") String token,
                                                   @PathVariable String email,
                                                   @RequestBody @Valid Employee employee) {
        setTenantSchemaFromToken(token);
        Employee updatedEmployee = employeeService.updateEmployee(email, employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Delete Employee by Email (Requires JWT Token)
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteEmployee(@RequestHeader("Authorization") String token,
                                                 @PathVariable String email) {
        setTenantSchemaFromToken(token);
        employeeService.deleteEmployee(email);
        return ResponseEntity.ok("✅ Employee deleted successfully!");
    }

    /**
     * Get All Employees (Requires JWT Token)
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(@RequestHeader("Authorization") String token) {
        setTenantSchemaFromToken(token);
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
}

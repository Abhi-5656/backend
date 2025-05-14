package com.wfm.experts.controller;

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import com.wfm.experts.validation.groups.OnAdminCreation;
import com.wfm.experts.validation.groups.OnEmployeeProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Employee Controller - Provides CRUD APIs for Employees.
 * Automatically switches to the correct schema based on JWT token.
 */
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TenantSchemaUtil tenantSchemaUtil;

    private void setTenantSchemaFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String tenantId = jwtUtil.extractTenantId(token);
        TenantContext.setTenant(tenantId);
        tenantSchemaUtil.ensureTenantSchemaIsSet();
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestHeader("Authorization") String token,
                                                   @Validated(OnEmployeeProfile.class) @RequestBody Employee employee) {
        setTenantSchemaFromToken(token);
        Employee savedEmployee = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    /**
     * Create multiple new Employees (Bulk Creation).
     * Validates each employee in the list against the OnEmployeeProfile group.
     */
    // Change mapping from "/bulk" to "/multi-create"
    @PostMapping("/multi-create") // <<<<------ MODIFIED HERE
    public ResponseEntity<List<Employee>> createMultipleEmployees(
            @RequestHeader("Authorization") String token,
            @Validated(OnEmployeeProfile.class) @RequestBody List<@Valid Employee> employees) {
        setTenantSchemaFromToken(token);
        List<Employee> createdEmployees = employeeService.createMultipleEmployees(employees);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployees);
    }


    @GetMapping("/{email}")
    public ResponseEntity<Employee> getEmployeeByEmail(@RequestHeader("Authorization") String token,
                                                       @PathVariable String email) {
        setTenantSchemaFromToken(token);
        Optional<Employee> employee = employeeService.getEmployeeByEmail(email);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{email}")
    public ResponseEntity<Employee> updateEmployee(@RequestHeader("Authorization") String token,
                                                   @PathVariable String email,
                                                   @Validated(OnEmployeeProfile.class) @RequestBody Employee employee) {
        setTenantSchemaFromToken(token);
        Employee updatedEmployee = employeeService.updateEmployee(email, employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@RequestHeader("Authorization") String token,
                                               @PathVariable String email) {
        setTenantSchemaFromToken(token);
        employeeService.deleteEmployee(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(@RequestHeader("Authorization") String token) {
        setTenantSchemaFromToken(token);
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
}
package com.wfm.experts.tenant.common.employees.controller;

import com.wfm.experts.security.JwtUtil;
import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import com.wfm.experts.tenancy.TenantContext;
import com.wfm.experts.util.TenantSchemaUtil;
import com.wfm.experts.validation.groups.OnEmployeeProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@Validated // Ensures the controller can process validation
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
    @PreAuthorize("hasAuthority('employee:create')")
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestHeader("Authorization") String token,
                                                      @Validated(OnEmployeeProfile.class) @RequestBody EmployeeDTO employeeDTO) { // @Validated with group
        setTenantSchemaFromToken(token);
        EmployeeDTO savedEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    @PostMapping("/multi-create")
    @PreAuthorize("hasAuthority('employee:create')")
    public ResponseEntity<List<EmployeeDTO>> createMultipleEmployees(
            @RequestHeader("Authorization") String token,
            @Validated(OnEmployeeProfile.class) @RequestBody List<@Valid EmployeeDTO> employees) { // @Validated with group and @Valid
        setTenantSchemaFromToken(token);
        List<EmployeeDTO> createdEmployees = employeeService.createMultipleEmployees(employees);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployees);
    }

    @PutMapping("/multi-update")
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<List<EmployeeDTO>> updateMultipleEmployees(
            @RequestHeader("Authorization") String token,
            @Validated(OnEmployeeProfile.class) @RequestBody List<@Valid EmployeeDTO> employees) { // @Validated with group and @Valid
        setTenantSchemaFromToken(token);
        List<EmployeeDTO> updatedEmployees = employeeService.updateMultipleEmployees(employees);
        return ResponseEntity.ok(updatedEmployees);
    }


    @GetMapping("/ids")
    @PreAuthorize("hasAuthority('employee:readAll')")
    public ResponseEntity<List<String>> getAllEmployeeIds(@RequestHeader("Authorization") String token) {
        setTenantSchemaFromToken(token);
        List<String> employeeIds = employeeService.getAllEmployeeIds();
        return ResponseEntity.ok(employeeIds);
    }





//    @GetMapping("/{email}")
//    @PreAuthorize("hasAuthority('employee:read') or hasPermission(#email, 'EmployeeDTO', 'self')")
//    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@RequestHeader("Authorization") String token,
//                                                          @PathVariable String email) {
//        setTenantSchemaFromToken(token);
//        Optional<EmployeeDTO> employee = employeeService.getEmployeeByEmail(email);
//        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }

    @PutMapping("/{email}")
    @PreAuthorize("hasAuthority('employee:update') or hasPermission(#email, 'EmployeeDTO', 'self')")
    public ResponseEntity<EmployeeDTO> updateEmployee(@RequestHeader("Authorization") String token,
                                                      @PathVariable String email,
                                                      @Validated(OnEmployeeProfile.class) @RequestBody EmployeeDTO employeeDTO) { // @Validated with group
        setTenantSchemaFromToken(token);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(email, employeeDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasAuthority('employee:delete')")
    public ResponseEntity<Void> deleteEmployee(@RequestHeader("Authorization") String token,
                                               @PathVariable String email) {
        setTenantSchemaFromToken(token);
        employeeService.deleteEmployee(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employee:readAll')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(@RequestHeader("Authorization") String token) {
        setTenantSchemaFromToken(token);
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{employeeId}")
//    @PreAuthorize("hasAuthority('employee:read') or hasPermission(#employeeId, 'EmployeeDTO', 'self')")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmployeeId(
            @RequestHeader("Authorization") String token,
            @PathVariable String employeeId) {
        setTenantSchemaFromToken(token);
        Optional<EmployeeDTO> employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
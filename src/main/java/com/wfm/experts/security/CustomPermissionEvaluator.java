package com.wfm.experts.security;

import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.service.EmployeeService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final EmployeeService employeeService;

    public CustomPermissionEvaluator(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ((authentication == null) || (targetDomainObject == null) || !(permission instanceof String)) {
            return false;
        }

        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();

        if ("EMPLOYEEDTO".equals(targetType)) {
            EmployeeDTO employee = (EmployeeDTO) targetDomainObject;
            String permissionStr = (String) permission;

            // ABAC: Check if the user is trying to access their own record
            if ("self".equals(permissionStr)) {
                return employee.getEmail().equals(authentication.getName());
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ((authentication == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }

        if ("EmployeeDTO".equalsIgnoreCase(targetType)) {
            // Use the employee service to fetch the employee by email (which is the targetId in this case)
            Optional<EmployeeDTO> employeeOpt = employeeService.getEmployeeByEmail((String) targetId);
            if (employeeOpt.isPresent()) {
                // Delegate to the other hasPermission method
                return hasPermission(authentication, employeeOpt.get(), permission);
            }
        }
        return false;
    }
}
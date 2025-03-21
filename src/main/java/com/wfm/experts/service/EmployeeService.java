package com.wfm.experts.service;

import com.wfm.experts.entity.tenant.common.Employee;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;
import java.util.Optional;

/**
 * ✅ Service interface for Employee operations.
 */
public interface EmployeeService extends UserDetailsService {

    /**
     * ✅ Create a new employee.
     */
    Employee createEmployee(Employee employee);

    /**
     * ✅ Get an employee by email.
     */
    Optional<Employee> getEmployeeByEmail(String email);

    /**
     * ✅ Get all employees.
     */
    List<Employee> getAllEmployees();

    /**
     * ✅ Update an employee by email.
     */
    Employee updateEmployee(String email, Employee updatedEmployee);

    /**
     * ✅ Delete an employee by email.
     */
    void deleteEmployee(String email);
}

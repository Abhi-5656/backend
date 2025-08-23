package com.wfm.experts.service;

import com.wfm.experts.tenant.common.employees.entity.Employee;
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
     * ✅ Create multiple new employees.
     * @param employees List of Employee objects to create.
     * @return List of created Employee objects.
     */
    List<Employee> createMultipleEmployees(List<Employee> employees);

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
//    Employee updateEmployee(String email, Employee updatedEmployee);

    /**
     * ✅ Get all employee IDs.
     */
    List<String> getAllEmployeeIds();

    /**
     * ✅ Delete an employee by email.
     */
    void deleteEmployee(String email);

    /**
     * ✅ Get an employee by employee ID (code).
     */
    Optional<Employee> getEmployeeByEmployeeId(String employeeId);  // ✅ New method
}
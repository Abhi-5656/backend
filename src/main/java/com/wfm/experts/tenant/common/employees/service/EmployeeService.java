package com.wfm.experts.tenant.common.employees.service;

import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
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
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    /**
     * ✅ Create multiple new employees.
     * @param employeeDTOs List of EmployeeDTO objects to create.
     * @return List of created EmployeeDTO objects.
     */
    List<EmployeeDTO> createMultipleEmployees(List<EmployeeDTO> employeeDTOs);

    /**
     * ✅ Get an employee by email.
     */
    Optional<EmployeeDTO> getEmployeeByEmail(String email);

    /**
     * ✅ Get all employees.
     */
    List<EmployeeDTO> getAllEmployees();

    /**
     * ✅ Update an employee by email.
     */
    EmployeeDTO updateEmployee(String email, EmployeeDTO updatedEmployeeDTO);

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
    Optional<EmployeeDTO> getEmployeeByEmployeeId(String employeeId);
}
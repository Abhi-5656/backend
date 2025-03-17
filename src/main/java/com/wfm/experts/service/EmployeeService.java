package com.wfm.experts.service;

import com.wfm.experts.entity.tenant.common.Employee;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    /**
     * ✅ Authenticate an Employee using Email & Password in a given Tenant
     */
    @Transactional
    Employee authenticateByEmail(String email, String password);

    /**
     * ✅ Create a new Employee
     */
    @Transactional
    Employee createEmployee(Employee employee);

    /**
     * ✅ Get an Employee by Email
     */
    @Transactional(readOnly = true)
    Employee getEmployeeByEmail(String email);

    /**
     * ✅ Update an Employee
     */
    @Transactional
    Employee updateEmployee(String email, Employee employee);

    /**
     * ✅ Delete an Employee
     */
    @Transactional
    void deleteEmployee(String email);

    /**
     * ✅ Get All Employees
     */
    @Transactional(readOnly = true)
    List<Employee> getAllEmployees();
}

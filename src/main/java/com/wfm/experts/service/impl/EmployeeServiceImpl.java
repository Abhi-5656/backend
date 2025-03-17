package com.wfm.experts.service.impl;

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.repository.tenant.common.EmployeeRepository;
import com.wfm.experts.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public Employee authenticateByEmail(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found in Tenant Schema!"));

        if (!passwordEncoder.matches(password, employee.getPassword())) {
            throw new RuntimeException("❌ Invalid credentials!");
        }
        return employee;
    }

    @Transactional
    @Override
    public Employee createEmployee(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword())); // Encrypt password
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found!"));
    }

    @Transactional
    @Override
    public Employee updateEmployee(String email, Employee updatedEmployee) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found!"));

        existingEmployee.setFirstName(updatedEmployee.getFirstName());
        existingEmployee.setLastName(updatedEmployee.getLastName());
        existingEmployee.setPhoneNumber(updatedEmployee.getPhoneNumber());

        return employeeRepository.save(existingEmployee);
    }

    @Transactional
    @Override
    public void deleteEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Employee not found!"));
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
}

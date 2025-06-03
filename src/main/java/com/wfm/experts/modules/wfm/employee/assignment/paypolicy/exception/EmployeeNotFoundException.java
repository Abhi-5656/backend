package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String employeeId) {
        super("Employee not found with ID: " + employeeId);
    }
}

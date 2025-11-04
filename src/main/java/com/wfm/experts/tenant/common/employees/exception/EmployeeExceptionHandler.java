package com.wfm.experts.tenant.common.employees.exception;

import com.wfm.experts.exception.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for the Employee module.
 * It handles specific employee-related exceptions and maps them to appropriate HTTP responses.
 */
@RestControllerAdvice(basePackages = "com.wfm.experts.tenant.common.employees.controller")
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this handler takes precedence over general ones
public class EmployeeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException (404).
     * This is already annotated with @ResponseStatus(HttpStatus.NOT_FOUND),
     * but catching it here allows for consistent logging and JSON response format.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles EmployeeAlreadyExistsException (409).
     */
    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleEmployeeAlreadyExists(EmployeeAlreadyExistsException ex) {
        logger.warn("Employee creation/update conflict: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * Handles generic EmployeeServiceException (400).
     */
    @ExceptionHandler(EmployeeServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleEmployeeServiceException(EmployeeServiceException ex) {
        logger.warn("Employee service error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches database-level unique constraint violations (409).
     * This provides user-friendly messages instead of exposing database internals.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "A database constraint was violated. Check for duplicate unique fields.";
        String causeMsg = (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null)
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (causeMsg != null) {
            // Check top-level Employee constraints
            if (causeMsg.contains("uc_employees_email") || causeMsg.contains("employees_email_key")) {
                message = "An employee with this email already exists.";
            } else if (causeMsg.contains("uc_employees_employee_id") || causeMsg.contains("employees_employee_id_key")) {
                message = "An employee with this Employee ID already exists.";
            } else if (causeMsg.contains("uc_employees_phone_number") || causeMsg.contains("employees_phone_number_key")) {
                message = "An employee with this phone number already exists.";

                // --- NEW CHECKS for PersonalInfo constraints ---
            } else if (causeMsg.contains("employee_personal_info_pan_number_key")) {
                message = "An employee with this PAN Number already exists.";
            } else if (causeMsg.contains("employee_personal_info_aadhaar_number_key")) {
                message = "An employee with this Aadhaar Number already exists.";
            } else if (causeMsg.contains("employee_personal_info_personal_email_key")) {
                message = "An employee with this Personal Email already exists.";
            }
            // --- END NEW CHECKS ---
        }
        logger.warn("Data integrity violation: {}", message, ex);
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.CONFLICT);
    }

    /**
     * Handles bean validation (@Valid @RequestBody) failures (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed for @RequestBody: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * --- NEW HANDLER ---
     * Handles validation on method parameters (@Validated @PathVariable, @RequestParam, etc.) (400).
     * This is the one that fixes your 500 error.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        // Extracts the field name
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            // This logic correctly gets the field name from a path like "createMultipleEmployees.employees[0].email"
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        // In case of duplicate keys (e.g., two errors on one field), just take the first
                        (message1, message2) -> message1
                ));
        logger.warn("Validation failed for method parameter: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    // --- END NEW HANDLER ---

    /**
     * A catch-all for any other unexpected errors in the employee module (500).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred in the employee module", ex);
        return new ResponseEntity<>(new ErrorResponse("An unexpected internal server error occurred. Please contact support."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
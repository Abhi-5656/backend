package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.exception;

import com.wfm.experts.exception.ErrorResponse; // Import your ErrorResponse class
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Handles exceptions specifically for the Pay Policy Assignment module.
 * It is scoped to the paypolicy.controller package.
 */
@Order(Ordered.HIGHEST_PRECEDENCE) // Keeps priority over GlobalExceptionHandler
@RestControllerAdvice(basePackages = "com.wfm.experts.modules.wfm.employee.assignment.paypolicy.controller")
public class PayPolicyAssignmentExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles custom validation exceptions (400 Bad Request).
     */
    @ExceptionHandler(PayPolicyAssignmentValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePayPolicyAssignmentValidationException(PayPolicyAssignmentValidationException ex) {
        return createErrorResponse(ex.getMessage());
    }

    /**
     * Handles missing related resources (404 Not Found).
     */
    @ExceptionHandler(PayPolicyAssignmentResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePayPolicyAssignmentResourceNotFoundException(PayPolicyAssignmentResourceNotFoundException ex) {
        return createErrorResponse(ex.getMessage());
    }

    /**
     * Handles conflicts like date overlaps (409 Conflict).
     */
    @ExceptionHandler(DuplicatePayPolicyAssignmentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicatePayPolicyAssignmentException(DuplicatePayPolicyAssignmentException ex) {
        return createErrorResponse(ex.getMessage());
    }

    /**
     * Overrides the default handler for @Valid DTO binding errors.
     * This now returns an ErrorResponse body.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // Collect all field errors into a single string
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        // Use the ErrorResponse object as the body
        ErrorResponse errorResponse = new ErrorResponse(errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Helper method now returns your ErrorResponse object
    private ErrorResponse createErrorResponse(String message) {
        return new ErrorResponse(message);
    }
}
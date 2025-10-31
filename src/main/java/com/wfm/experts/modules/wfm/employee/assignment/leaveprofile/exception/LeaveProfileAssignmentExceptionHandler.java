package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.exception;

import com.wfm.experts.exception.ErrorResponse;
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
 * Handles exceptions specifically for the Leave Profile Assignment module.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.controller")
public class LeaveProfileAssignmentExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles custom validation exceptions (400 Bad Request).
     */
    @ExceptionHandler(LeaveProfileAssignmentValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(LeaveProfileAssignmentValidationException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles missing related resources (404 Not Found).
     */
    @ExceptionHandler(LeaveProfileAssignmentResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(LeaveProfileAssignmentResourceNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles conflicts like date overlaps (409 Conflict).
     */
    @ExceptionHandler(DuplicateLeaveProfileAssignmentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateAssignmentException(DuplicateLeaveProfileAssignmentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Overrides the default handler for @Valid DTO binding errors.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));


        return new ResponseEntity<>(new ErrorResponse(errors), HttpStatus.BAD_REQUEST);
    }
}
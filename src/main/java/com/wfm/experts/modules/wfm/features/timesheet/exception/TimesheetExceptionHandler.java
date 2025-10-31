package com.wfm.experts.modules.wfm.features.timesheet.exception;

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
 * Handles exceptions specifically for the Timesheet and PunchEvent module.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.wfm.experts.modules.wfm.features.timesheet.controller")
public class TimesheetExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles custom validation exceptions (400 Bad Request).
     */
    @ExceptionHandler(TimesheetValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(TimesheetValidationException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles general resource not found exceptions (e.g., Employee not found) (404 Not Found).
     */
    @ExceptionHandler(TimesheetResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(TimesheetResourceNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles existing Timesheet not found exception (404 Not Found).
     */
    @ExceptionHandler(TimesheetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTimesheetNotFoundException(TimesheetNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Handles existing PunchEvent not found exception (404 Not Found).
     */
    @ExceptionHandler(PunchEventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePunchEventNotFoundException(PunchEventNotFoundException ex) {
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
        String combinedMessage = "Validation failed for request body: " + errors;

        return new ResponseEntity<>(new ErrorResponse(combinedMessage), HttpStatus.BAD_REQUEST);
    }
}
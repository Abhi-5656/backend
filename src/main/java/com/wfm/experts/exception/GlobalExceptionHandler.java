package com.wfm.experts.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to catch common application-wide exceptions.
 * This handler catches exceptions not handled by more specific, module-level handlers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles DTO validation failures (e.g., @NotBlank, @NotNull, @Min).
     * Returns a 400 Bad Request with a map of fields and their error messages.
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
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles JSON parsing errors, such as invalid enum values or incorrect date formats.
     * Returns a 400 Bad Request with a specific error message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Invalid request body format.";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;

            String fieldName = ife.getPath().stream()
                    .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String invalidValue = ife.getValue().toString();
                String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                message = String.format("Invalid value '%s' for field '%s'. Must be one of: [%s]",
                        invalidValue, fieldName, validValues);
            }
            else if (ife.getTargetType() == LocalDate.class || (ife.getMessage() != null && ife.getMessage().contains("java.time.LocalDate"))) {
                message = String.format("Invalid date format for field '%s'. Dates must be in YYYY-MM-DD format.", fieldName);
            }
        } else if (cause != null && cause.getCause() instanceof DateTimeParseException) {
            message = "Invalid date format. Dates must be in YYYY-MM-DD format.";
        }

        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles database integrity violations (e.g., unique constraints, not-null constraints).
     * Returns a 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation. This could be due to a duplicate entry or a foreign key constraint failure.";
        if (ex.getCause() != null && ex.getCause().getMessage().contains("violates not-null constraint")) {
            message = "A required field was left null.";
        } else if (ex.getCause() != null && ex.getCause().getMessage().contains("violates unique constraint")) {
            message = "A record with this name or key already exists.";
        } else if (ex.getCause() != null && ex.getCause().getMessage().contains("violates foreign key constraint")) {
            message = "A reference to a non-existent record was made.";
        }
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.CONFLICT);
    }

    /**
     * A catch-all handler for any other unexpected exceptions.
     * Returns a 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full stack trace for debugging
        ex.printStackTrace();

        return new ResponseEntity<>(new ErrorResponse("An unexpected internal server error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
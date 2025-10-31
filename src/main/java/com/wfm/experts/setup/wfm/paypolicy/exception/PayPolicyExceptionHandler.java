package com.wfm.experts.setup.wfm.paypolicy.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.wfm.experts.exception.ErrorResponse;
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

@ControllerAdvice
public class PayPolicyExceptionHandler {

    @ExceptionHandler(PayPolicyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handlePayPolicyNotFoundException(PayPolicyNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PayPolicyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handlePayPolicyAlreadyExistsException(PayPolicyAlreadyExistsException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation. This could be due to a duplicate entry or a foreign key constraint failure.";
        if (ex.getCause() != null && ex.getCause().getMessage().contains("violates not-null constraint")) {
            message = "A required field was left null.";
        } else if (ex.getCause() != null && ex.getCause().getMessage().contains("violates unique constraint")) {
            message = "A record with the same unique value already exists.";
        }
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Invalid request body format.";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;

            // Get the field name
            String fieldName = ife.getPath().stream()
                    .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));

            // Check if the error is related to an enum
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String invalidValue = ife.getValue().toString();

                // Get the list of valid enum values
                String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = String.format("Invalid value '%s' for field '%s'. Must be one of: [%s]",
                        invalidValue, fieldName, validValues);
            }
            // Check for LocalDate format error
            else if (ife.getTargetType() == LocalDate.class || (ife.getMessage() != null && ife.getMessage().contains("java.time.LocalDate"))) {
                message = String.format("Invalid date format for field '%s'. Dates must be in YYYY-MM-DD format.", fieldName);
            }
        } else if (cause != null && cause.getCause() instanceof DateTimeParseException) {
            // This is a fallback for date parsing
            message = "Invalid date format. Dates must be in YYYY-MM-DD format.";
        }

        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

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
}
package com.wfm.experts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ✅ Global Exception Handler for Authentication and JWT-related errors.
 * Updated to use standard ErrorResponse class.
 */
@RestControllerAdvice
public class JwtExceptionHandler {

    /**
     * ✅ Handles JWT Authentication Exceptions.
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * ✅ Handles Empty Username Exception.
     */
    @ExceptionHandler(EmptyUsernameException.class)
    public ResponseEntity<ErrorResponse> handleEmptyUsernameException(EmptyUsernameException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * ✅ Handles Empty Password Exception.
     */
    @ExceptionHandler(EmptyPasswordException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPasswordException(EmptyPasswordException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * ✅ Handles Null Credentials Exception (when username or password is null).
     */
    @ExceptionHandler(NullCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleNullCredentialsException(NullCredentialsException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * ✅ Handles Invalid Email Exception.
     */
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailException(InvalidEmailException ex) {
        // Note: This was HttpStatus.NOT_FOUND, but for auth failure, 401 is more common.
        // Keeping NOT_FOUND as per original file.
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * ✅ Handles Invalid Password Exception.
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * ✅ Generic method to create structured JSON error responses.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message), status);
    }
}
package com.erasm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handling for the whole application.
 * Converts custom and framework exceptions into a consistent JSON error body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse buildError(HttpStatus status, String message, WebRequest request) {
        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        logger.warn("User not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex, WebRequest request) {
        logger.warn("Employee not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException ex, WebRequest request) {
        logger.warn("Project not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SkillNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSkillNotFound(SkillNotFoundException ex, WebRequest request) {
        logger.warn("Skill not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceRequestNotFound(ResourceRequestNotFoundException ex,
            WebRequest request) {
        logger.warn("Resource request not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CertificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCertificationNotFound(CertificationNotFoundException ex,
            WebRequest request) {
        logger.warn("Certification not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeSkillNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeSkillNotFound(EmployeeSkillNotFoundException ex,
            WebRequest request) {
        logger.warn("Employee skill record not found: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AllocationException.class)
    public ResponseEntity<ErrorResponse> handleAllocationException(AllocationException ex, WebRequest request) {
        logger.warn("Invalid allocation request: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidWorkflowStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWorkflow(InvalidWorkflowStateException ex, WebRequest request) {
        logger.warn("Invalid workflow transition: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, WebRequest request) {
        logger.warn("Duplicate resource: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.CONFLICT, ex.getMessage(), request), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        logger.warn("Invalid login attempt: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password", request),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        logger.warn("Unauthorized access attempt: {}", ex.getMessage());
        return new ResponseEntity<>(
                buildError(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = buildError(HttpStatus.BAD_REQUEST, "Validation failed", request);
        errorResponse.setValidationErrors(fieldErrors);
        logger.warn("Validation failed: {}", fieldErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unhandled system exception occurred", ex);
        return new ResponseEntity<>(
                buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
                        request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

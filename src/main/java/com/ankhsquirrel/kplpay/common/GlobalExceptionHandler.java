package com.ankhsquirrel.kplpay.common;

import com.ankhsquirrel.kplpay.customer.DuplicateSiretException;
import com.ankhsquirrel.kplpay.integration.insee.InseeUnavailableException;
import com.ankhsquirrel.kplpay.integration.insee.SiretNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Central translation of application exceptions into {@link ApiError} responses with the right
 * HTTP status. Controllers never catch these themselves.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed");
    }

    @ExceptionHandler(DuplicateSiretException.class)
    public ResponseEntity<ApiError> handleDuplicateSiret(DuplicateSiretException ex) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_SIRET", ex.getMessage());
    }

    @ExceptionHandler(SiretNotFoundException.class)
    public ResponseEntity<ApiError> handleSiretNotFound(SiretNotFoundException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "SIRET_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InseeUnavailableException.class)
    public ResponseEntity<ApiError> handleInseeUnavailable(InseeUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "INSEE_UNAVAILABLE",
                "Could not reach the INSEE Sirene registry, please retry later");
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message));
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                                  List<ApiError.FieldError> errors) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, errors));
    }
}

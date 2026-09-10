package com.ankhsquirrel.kplpay.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error body returned for every handled exception.
 *
 * @param timestamp when the error was produced
 * @param status    HTTP status code
 * @param code      stable, machine-readable error code (e.g. {@code VALIDATION_ERROR})
 * @param message   human-readable summary
 * @param errors    per-field details, present only for validation failures
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<FieldError> errors) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(Instant.now(), status, code, message, List.of());
    }

    public static ApiError of(int status, String code, String message, List<FieldError> errors) {
        return new ApiError(Instant.now(), status, code, message, errors);
    }

    public record FieldError(String field, String message) {
    }
}

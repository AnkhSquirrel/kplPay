package com.ankhsquirrel.kplpay.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

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

        @Schema(description = "When the error was produced")
        Instant timestamp,

        @Schema(description = "HTTP status code")
        int status,

        @Schema(description = "Stable, machine-readable error code (e.g. VALIDATION_ERROR, CUSTOMER_NOT_FOUND)")
        String code,

        @Schema(description = "Human-readable summary of the error")
        String message,

        @Schema(description = "Per-field validation details, present only for VALIDATION_ERROR")
        List<FieldError> errors) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(Instant.now(), status, code, message, List.of());
    }

    public static ApiError of(int status, String code, String message, List<FieldError> errors) {
        return new ApiError(Instant.now(), status, code, message, errors);
    }

    public record FieldError(

            @Schema(description = "Name of the invalid request field", example = "fieldName")
            String field,

            @Schema(description = "Why the field failed validation", example = "validation failure reason")
            String message) {
    }
}

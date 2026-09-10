package com.ankhsquirrel.kplpay.integration.insee;

/**
 * Thrown when the INSEE Sirene API cannot be reached or fails unexpectedly (connection error,
 * timeout, or a 5xx response).
 */
public class InseeUnavailableException extends RuntimeException {

    public InseeUnavailableException(Throwable cause) {
        super("INSEE Sirene API is unavailable", cause);
    }
}

package com.ankhsquirrel.kplpay.integration.insee;

import java.io.Serial;

/**
 * Thrown when the INSEE Sirene API cannot be reached or fails unexpectedly (connection error,
 * timeout, or a 5xx response).
 */
public class InseeUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InseeUnavailableException(Throwable cause) {
        super("INSEE Sirene API is unavailable", cause);
    }
}

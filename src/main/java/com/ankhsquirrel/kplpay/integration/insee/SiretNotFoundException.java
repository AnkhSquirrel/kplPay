package com.ankhsquirrel.kplpay.integration.insee;

import lombok.Getter;

import java.io.Serial;

/**
 * Thrown when the INSEE Sirene registry has no établissement for the given SIRET (HTTP 404).
 */
@Getter
public class SiretNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String siret;

    public SiretNotFoundException(String siret) {
        super("SIRET not found in the Sirene registry: " + siret);
        this.siret = siret;
    }

}

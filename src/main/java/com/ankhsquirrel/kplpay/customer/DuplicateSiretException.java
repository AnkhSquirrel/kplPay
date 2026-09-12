package com.ankhsquirrel.kplpay.customer;

import lombok.Getter;

import java.io.Serial;

/**
 * Thrown when a customer with the given SIRET is already registered.
 */
@Getter
public class DuplicateSiretException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String siret;

    public DuplicateSiretException(String siret) {
        super("A customer is already registered with SIRET " + siret);
        this.siret = siret;
    }

}

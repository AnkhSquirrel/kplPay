package com.ankhsquirrel.kplpay.customer;

import lombok.Getter;

/**
 * Thrown when a customer with the given SIRET is already registered.
 */
@Getter
public class DuplicateSiretException extends RuntimeException {

    private final String siret;

    public DuplicateSiretException(String siret) {
        super("A customer is already registered with SIRET " + siret);
        this.siret = siret;
    }

}

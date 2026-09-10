package com.ankhsquirrel.kplpay.customer;

import lombok.Getter;

import java.util.UUID;

/**
 * Thrown when an operation references a customer id that does not exist.
 */
@Getter
public class CustomerNotFoundException extends RuntimeException {

    private final UUID customerId;

    public CustomerNotFoundException(UUID customerId) {
        super("No customer found with id " + customerId);
        this.customerId = customerId;
    }

}

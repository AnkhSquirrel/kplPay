package com.ankhsquirrel.kplpay.subscription;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

/**
 * Thrown when an operation references a subscription id that does not exist.
 */
@Getter
public class SubscriptionNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID subscriptionId;

    public SubscriptionNotFoundException(UUID subscriptionId) {
        super("No subscription found with id " + subscriptionId);
        this.subscriptionId = subscriptionId;
    }

}

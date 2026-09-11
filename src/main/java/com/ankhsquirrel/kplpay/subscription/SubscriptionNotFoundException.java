package com.ankhsquirrel.kplpay.subscription;

import lombok.Getter;

import java.util.UUID;

/**
 * Thrown when an operation references a subscription id that does not exist.
 */
@Getter
public class SubscriptionNotFoundException extends RuntimeException {

    private final UUID subscriptionId;

    public SubscriptionNotFoundException(UUID subscriptionId) {
        super("No subscription found with id " + subscriptionId);
        this.subscriptionId = subscriptionId;
    }

}

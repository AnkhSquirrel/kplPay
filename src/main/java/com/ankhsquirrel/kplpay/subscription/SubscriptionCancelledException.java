package com.ankhsquirrel.kplpay.subscription;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

/**
 * Thrown when an operation that requires an active subscription (e.g. invoice generation) is
 * attempted against one that has been cancelled. Distinct from
 * {@link SubscriptionNotFoundException}: the subscription exists, its state just disallows the
 * operation — a 409, not a 404.
 */
@Getter
public class SubscriptionCancelledException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID subscriptionId;

    public SubscriptionCancelledException(UUID subscriptionId) {
        super("Subscription " + subscriptionId + " is cancelled and cannot be invoiced");
        this.subscriptionId = subscriptionId;
    }

}
